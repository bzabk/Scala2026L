import json
import boto3
from moto import mock_aws
from unittest.mock import patch

with patch("boto3.resource"):
    import recent_games
    import save_game


def make_event(body: dict) -> dict:
    return {"body": json.dumps(body)}


def make_recent_event(username: str) -> dict:
    return {"queryStringParameters": {"username": username}}


def create_games_table(ddb):
    return ddb.create_table(
        TableName="Games",
        KeySchema=[
            {"AttributeName": "username", "KeyType": "HASH"},
            {"AttributeName": "createdAt", "KeyType": "RANGE"},
        ],
        AttributeDefinitions=[
            {"AttributeName": "username", "AttributeType": "S"},
            {"AttributeName": "createdAt", "AttributeType": "S"},
        ],
        BillingMode="PAY_PER_REQUEST",
    )


def test_save_game_success():
    with mock_aws():
        ddb = boto3.resource("dynamodb", region_name="us-east-1")
        table = create_games_table(ddb)
        with patch.object(save_game, "table", table):
            response = save_game.lambda_handler(make_event({
                "username": "user1",
                "createdAt": "2026-06-05T11:00:00Z",
                "difficulty": "Easy",
                "elapsedSeconds": 12,
                "board": [[0 for _ in range(9)] for _ in range(9)],
                "status": "in_progress",
            }), None)

            assert response["statusCode"] == 200
            item = table.get_item(Key={
                "username": "user1",
                "createdAt": "2026-06-05T11:00:00Z",
            })["Item"]
            assert item["difficulty"] == "Easy"
            assert item["elapsedSeconds"] == 12


def test_save_game_missing_body():
    with mock_aws():
        ddb = boto3.resource("dynamodb", region_name="us-east-1")
        table = create_games_table(ddb)
        with patch.object(save_game, "table", table):
            response = save_game.lambda_handler({}, None)

            assert response["statusCode"] == 400


def test_save_game_defaults_resume_fields():
    board = [[0 for _ in range(9)] for _ in range(9)]
    board[0][0] = 5

    with mock_aws():
        ddb = boto3.resource("dynamodb", region_name="us-east-1")
        table = create_games_table(ddb)
        with patch.object(save_game, "table", table):
            response = save_game.lambda_handler(make_event({
                "username": "user1",
                "createdAt": "2026-06-05T12:00:00Z",
                "board": board,
            }), None)

            assert response["statusCode"] == 200
            item = table.get_item(Key={
                "username": "user1",
                "createdAt": "2026-06-05T12:00:00Z",
            })["Item"]
            assert item["gameId"] == "user1-2026-06-05T12:00:00Z"
            assert item["difficulty"] == "Easy"
            assert item["initialBoard"] == board
            assert item["status"] == "in_progress"
            assert item["errorCount"] == 0
            assert item["hintsRemaining"] == 3
            assert len(item["notes"]) == 9


def test_save_game_requires_board():
    with mock_aws():
        ddb = boto3.resource("dynamodb", region_name="us-east-1")
        table = create_games_table(ddb)
        with patch.object(save_game, "table", table):
            response = save_game.lambda_handler(make_event({"username": "user1"}), None)

            assert response["statusCode"] == 400


def test_save_game_preserves_explicit_resume_fields():
    board = [[0 for _ in range(9)] for _ in range(9)]
    initial_board = [[0 for _ in range(9)] for _ in range(9)]
    initial_board[0][0] = 5
    notes = [[[] for _ in range(9)] for _ in range(9)]
    notes[0][1] = [2, 4]

    with mock_aws():
        ddb = boto3.resource("dynamodb", region_name="us-east-1")
        table = create_games_table(ddb)
        with patch.object(save_game, "table", table):
            response = save_game.lambda_handler(make_event({
                "username": "user1",
                "createdAt": "2026-06-05T13:00:00Z",
                "gameId": "game-explicit",
                "difficulty": "Expert",
                "elapsedSeconds": 321,
                "board": board,
                "initialBoard": initial_board,
                "notes": notes,
                "status": "finished",
                "errorCount": 2,
                "hintsRemaining": 1,
            }), None)

            assert response["statusCode"] == 200
            item = table.get_item(Key={
                "username": "user1",
                "createdAt": "2026-06-05T13:00:00Z",
            })["Item"]
            assert item["gameId"] == "game-explicit"
            assert item["difficulty"] == "Expert"
            assert item["elapsedSeconds"] == 321
            assert item["initialBoard"] == initial_board
            assert item["notes"][0][1] == [2, 4]
            assert item["status"] == "finished"
            assert item["errorCount"] == 2
            assert item["hintsRemaining"] == 1


def test_recent_games_returns_latest_first():
    with mock_aws():
        ddb = boto3.resource("dynamodb", region_name="us-east-1")
        table = create_games_table(ddb)
        table.put_item(Item={
            "username": "user1",
            "createdAt": "2026-06-05T10:00:00Z",
            "difficulty": "Easy",
            "elapsedSeconds": 10,
            "board": [[0 for _ in range(9)] for _ in range(9)],
            "status": "in_progress",
        })
        table.put_item(Item={
            "username": "user1",
            "createdAt": "2026-06-05T11:00:00Z",
            "difficulty": "Hard",
            "elapsedSeconds": 20,
            "board": [[0 for _ in range(9)] for _ in range(9)],
            "status": "finished",
        })

        with patch.object(recent_games, "table", table):
            response = recent_games.lambda_handler(make_recent_event("user1"), None)

            assert response["statusCode"] == 200
            games = json.loads(response["body"])
            assert [game["difficulty"] for game in games] == ["Hard", "Easy"]


def test_recent_games_requires_username():
    with mock_aws():
        ddb = boto3.resource("dynamodb", region_name="us-east-1")
        table = create_games_table(ddb)
        with patch.object(recent_games, "table", table):
            response = recent_games.lambda_handler({"queryStringParameters": {}}, None)

            assert response["statusCode"] == 400


def test_recent_games_handles_missing_query_parameters():
    with mock_aws():
        ddb = boto3.resource("dynamodb", region_name="us-east-1")
        table = create_games_table(ddb)
        with patch.object(recent_games, "table", table):
            response = recent_games.lambda_handler({}, None)

            assert response["statusCode"] == 400


def test_recent_games_limits_to_latest_six_and_filters_user():
    with mock_aws():
        ddb = boto3.resource("dynamodb", region_name="us-east-1")
        table = create_games_table(ddb)
        for index in range(8):
            table.put_item(Item={
                "username": "user1",
                "createdAt": f"2026-06-05T1{index}:00:00Z",
                "difficulty": f"Game {index}",
                "elapsedSeconds": index,
                "board": [[0 for _ in range(9)] for _ in range(9)],
                "status": "in_progress",
            })
        table.put_item(Item={
            "username": "user2",
            "createdAt": "2026-06-05T19:00:00Z",
            "difficulty": "Other user",
            "elapsedSeconds": 99,
            "board": [[0 for _ in range(9)] for _ in range(9)],
            "status": "finished",
        })

        with patch.object(recent_games, "table", table):
            response = recent_games.lambda_handler(make_recent_event("user1"), None)

            assert response["statusCode"] == 200
            games = json.loads(response["body"])
            assert len(games) == 6
            assert [game["difficulty"] for game in games] == [
                "Game 7",
                "Game 6",
                "Game 5",
                "Game 4",
                "Game 3",
                "Game 2",
            ]
            assert all(game["difficulty"] != "Other user" for game in games)
