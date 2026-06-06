import json
import boto3
import hashlib

dynamodb = boto3.resource('dynamodb')
table = dynamodb.Table('Users')

def hash_password(password, salt="sudoku_salt_2024"):
    return hashlib.sha256((salt + password).encode('utf-8')).hexdigest()

def build_response(status_code, message, data=None):
    body = {"message": message}
    if data:
        body["data"] = data
    return {
        'statusCode': status_code,
        'headers': {
            'Content-Type': 'application/json',
            'Access-Control-Allow-Origin': '*'
        },
        'body': json.dumps(body)
    }

def lambda_handler(event, context):
    try:
        if 'body' not in event:
            return build_response(400, "Missing request body")

        body = json.loads(event['body'])
        username = body.get('username')
        password = body.get('password')

        if not username or not password:
            return build_response(400, "Missing username or password")

        user_response = table.get_item(Key={'username': username})
        if 'Item' not in user_response:
            return build_response(404, "User does not exist")

        stored_hash = user_response['Item'].get('passwordHash')
        if hash_password(password) == stored_hash:
            return build_response(200, "Logged in", {"username": username})
        else:
            return build_response(401, "Invalid Password")
    except Exception:
        return build_response(500, "Internal server error")
