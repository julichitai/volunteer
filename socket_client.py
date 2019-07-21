import json
import websockets
import asyncio


async def test():
    uri = "ws://192.168.0.30:8765"
    async with websockets.connect(uri) as websocket:
        data = {"jsonrpc": "2.0",
                "method": "get_all_volunteers",
                "params": [],
                "id": 0}

        await websocket.send(json.dumps(data))

        res = await websocket.recv()
        print(f"Recieve: {res}")


asyncio.get_event_loop().run_until_complete(test())