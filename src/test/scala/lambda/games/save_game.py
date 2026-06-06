import json
import time
import boto3

dynamodb = boto3.resource("dynamodb")
table = dynamodb.Table("Games")


def build_response(status_code, message, data=None):
    body = {"message": message}
    if data is not None:
        body["data"] = data

    return {
        "statusCode": status_code,
        "headers": {
            "Content-Type": "application/json",
            "Access-Control-Allow-Origin": "*",
        },
        "body": json.dumps(body),
    }


def lambda_handler(event, context):
    try:
        if "body" not in event:
            return build_response(400, "Missing request body.")

        body = json.loads(event["body"])
        username = body.get("username")
        board = body.get("board")

        if not username or board is None:
            return build_response(400, "Missing username or board.")

        created_at = body.get("createdAt") or str(int(time.time() * 1000))
        game_id = body.get("gameId") or f"{username}-{created_at}"

        item = {
            "username": username,
            "createdAt": created_at,
            "gameId": game_id,
            "difficulty": body.get("difficulty", "Easy"),
            "elapsedSeconds": int(body.get("elapsedSeconds", 0)),
            "board": board,
            "initialBoard": body.get("initialBoard", board),
            "notes": body.get("notes", [[[] for _ in range(9)] for _ in range(9)]),
            "status": body.get("status", "in_progress"),
            "errorCount": int(body.get("errorCount", 0)),
            "hintsRemaining": int(body.get("hintsRemaining", 3)),
            "updatedAt": int(time.time() * 1000),
        }

        table.put_item(Item=item)

        return build_response(200, "Game saved.", {"gameId": game_id})
    except Exception:
        return build_response(500, "Internal server error.")
