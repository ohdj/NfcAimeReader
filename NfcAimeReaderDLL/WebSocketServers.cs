using Fleck;

namespace NfcAimeReaderDLL;

public class WebSocketServers
{
    // Track all active WebSocket connections
    public static readonly List<IWebSocketConnection> ActiveConnections = new List<IWebSocketConnection>();

    public static WebSocketServer GetWebSocketServer()
    {
        var listenerAddress = Config.ServerAddress;
        var port = Config.ServerPort;
        var server = new WebSocketServer("ws://" + listenerAddress + ":" + port);

        return server;
    }

    // Helper method to broadcast a message to all connected clients
    public static void BroadcastMessage(object message)
    {
        string jsonMessage = Newtonsoft.Json.JsonConvert.SerializeObject(message);
        foreach (var socket in ActiveConnections)
        {
            socket.Send(jsonMessage);
        }
    }
}