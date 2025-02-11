using Fleck;

namespace NfcAimeReaderDLL;

public class WebSocketServers 
{
    public static WebSocketServer GetWebSocketServer()
    {
        var listenerAddress = Config.ServerAddress;
        var port = Config.ServerPort;
        var server = new WebSocketServer("ws://" + listenerAddress + ":" + port);
        return server;
    }
}