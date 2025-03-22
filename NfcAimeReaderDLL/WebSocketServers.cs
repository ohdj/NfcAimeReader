using Fleck;
using Microsoft.AspNetCore.Hosting.Server;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using System.Runtime.CompilerServices;


namespace NfcAimeReaderDLL;

public class WebSocketServers 
{
    static Card card;
    public static void RunWebSocketServer(Card cards) {
        card = cards;
        var listenerAddress = Config.ServerAddress;
        var port = Config.ServerPort;
        var server = new WebSocketServer("ws://" + listenerAddress + ":" + port);
        server.Start(socket =>
        {
            socket.OnOpen = () => Console.WriteLine("Clinet connected! Address:" + socket.ConnectionInfo.ClientIpAddress + ":" + socket.ConnectionInfo.ClientPort);
            socket.OnClose = () => Console.WriteLine("The connection had been lost!");
            socket.OnError = e => Console.WriteLine("Error: " + e);
            socket.OnMessage = async message =>
            {
                Console.WriteLine("Received message: " + message);
                try
                {
                   WebSocketPacketHandle(message);
                }
                catch (JsonReaderException)
                {
                    Console.WriteLine("Received message is not in JSON format.");
                }
                await Task.CompletedTask; // Ensure the method is awaited
            };
        });
    }

    public static void WebSocketPacketHandle(string message) {
        //var card = Card.card;

        //尝试解析为JSON
        JObject jsonObj = JObject.Parse(message);
        var Module = jsonObj["module"]?.ToString();
        //根据模式处理
        if (Module == "card")
        {
            var jsonParams = jsonObj["params"];
            if (jsonParams != null && jsonParams.Count() > 1)
            {
                string? targetValue = jsonParams[1]?.ToString();
                if (targetValue != null)
                {
                    Console.WriteLine("IDm: " + targetValue);
                    card.SetCardIdm(targetValue);
                }
            }
        }
        else if (Module == "access_code")
        {
            var jsonParams = jsonObj["params"];
            if (jsonParams != null && jsonParams.Count() > 1)
            {
                string? targetValue = jsonParams[1]?.ToString();
                if (targetValue.Length == 20)
                {
                    Console.WriteLine("AccessCode: " + targetValue);
                    card.SetCardAccessCode(targetValue);
                }
            }
        }
    }
}