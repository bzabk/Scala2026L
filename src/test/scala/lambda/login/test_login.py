import json
import pytest
import boto3
from moto import mock_aws
from unittest.mock import patch

with patch("boto3.resource"):
    import login


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
        with patch.object(login, "table", table):
            yield table


def make_event(body: dict) -> dict:
    return {"body": json.dumps(body)}


def test_login_success(users_table):
    users_table.put_item(Item={
        "username": "user1",
        "passwordHash": login.hash_password("secret123"),
        "email": "user1@example.com",
    })
    r = login.lambda_handler(make_event({"username": "user1", "password": "secret123"}), None)
    assert r["statusCode"] == 200
    assert json.loads(r["body"])["message"] == "Logged in"


def test_login_user_not_found(users_table):
    r = login.lambda_handler(make_event({"username": "ghost", "password": "pass"}), None)
    assert r["statusCode"] == 404


def test_login_wrong_password(users_table):
    users_table.put_item(Item={
        "username": "user1",
        "passwordHash": login.hash_password("correct"),
        "email": "user1@example.com",
    })
    r = login.lambda_handler(make_event({"username": "user1", "password": "wrong"}), None)
    assert r["statusCode"] == 401



def test_hash_password_is_deterministic():
    assert login.hash_password("abc") == login.hash_password("abc")


def test_hash_password_uses_salt():
    assert login.hash_password("abc") != login.hash_password("abc", salt="other_salt")
