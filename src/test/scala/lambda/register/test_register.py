import json
import pytest
import boto3
from moto import mock_aws
from unittest.mock import patch

with patch("boto3.resource"):
    import register


@pytest.fixture
def users_table():
    with mock_aws():
        ddb = boto3.resource("dynamodb", region_name="us-east-1")
        table = ddb.create_table(
            TableName="Users",
            KeySchema=[{"AttributeName": "username", "KeyType": "HASH"}],
            AttributeDefinitions=[{"AttributeName": "username", "AttributeType": "S"}],
            BillingMode="PAY_PER_REQUEST",
        )
        with patch.object(register, "table", table):
            yield table


def make_event(body: dict) -> dict:
    return {"body": json.dumps(body)}


def test_register_success(users_table):
    r = register.lambda_handler(make_event({"username": "newuser", "password": "pass123", "email": "a@b.com"}), None)
    assert r["statusCode"] == 200
    assert json.loads(r["body"])["message"] == "Zarejestrowano pomyślnie!"


def test_register_saves_hashed_password(users_table):
    register.lambda_handler(make_event({"username": "u", "password": "mypassword"}), None)
    item = users_table.get_item(Key={"username": "u"})["Item"]
    assert item["passwordHash"] == register.hash_password("mypassword")
    assert "mypassword" not in item.values()


def test_register_saves_email(users_table):
    register.lambda_handler(make_event({"username": "u", "password": "p", "email": "test@test.com"}), None)
    item = users_table.get_item(Key={"username": "u"})["Item"]
    assert item["email"] == "test@test.com"


def test_register_user_already_exists(users_table):
    users_table.put_item(Item={"username": "existing", "passwordHash": "hash"})
    r = register.lambda_handler(make_event({"username": "existing", "password": "pass"}), None)
    assert r["statusCode"] == 400


def test_register_missing_body(users_table):
    r = register.lambda_handler({}, None)
    assert r["statusCode"] == 400


def test_register_missing_username(users_table):
    r = register.lambda_handler(make_event({"password": "pass123"}), None)
    assert r["statusCode"] == 400


def test_register_missing_password(users_table):
    r = register.lambda_handler(make_event({"username": "user1"}), None)
    assert r["statusCode"] == 400


def test_register_response_has_cors_header(users_table):
    r = register.lambda_handler(make_event({"username": "u", "password": "p"}), None)
    assert r["headers"]["Access-Control-Allow-Origin"] == "*"
