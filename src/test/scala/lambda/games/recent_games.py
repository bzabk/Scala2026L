import json
import boto3
from boto3.dynamodb.conditions import Key
from decimal import Decimal

dynamodb = boto3.resource("dynamodb")
table = dynamodb.Table("Games")


def decimal_default(value):
    if isinstance(value, Decimal):
        if value % 1 == 0:
            return int(value)
        return float(value)
    raise TypeError


def build_response(status_code, body):
    return {
        "statusCode": status_code,
        "headers": {
            "Content-Type": "application/json",
            "Access-Control-Allow-Origin": "*",
        },
        "body": json.dumps(body, default=decimal_default),
    }


def lambda_handler(event, context):
    try:
        params = event.get("queryStringParameters") or {}
        username = params.get("username")

        if not username:
            return build_response(400, {"message": "Missing username."})

        response = table.query(
            KeyConditionExpression=Key("username").eq(username),
            ScanIndexForward=False,
            Limit=6,
        )

        return build_response(200, response.get("Items", []))
    except Exception:
        return build_response(500, {"message": "Internal server error."})
