import json
import boto3
import hashlib
import time

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
            return build_response(400, "Brak danych w zapytaniu (puste body).")

        body = json.loads(event['body'])
        username = body.get('username')
        password = body.get('password')
        email = body.get('email', '')

        if not username or not password:
            return build_response(400, "Brakuje loginu lub hasła.")

        existing_user = table.get_item(Key={'username': username})
        if 'Item' in existing_user:
            return build_response(400, "Użytkownik o takiej nazwie już istnieje.")

        created_at_ms = int(time.time() * 1000)

        table.put_item(Item={
            'username': username,
            'passwordHash': hash_password(password),
            'email': email,
            'createdAt': created_at_ms
        })
        return build_response(200, "Zarejestrowano pomyślnie!", {"username": username})

    except Exception as e:
        return build_response(500, "Wewnętrzny błąd serwera.")