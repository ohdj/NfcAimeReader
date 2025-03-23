using Fleck;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using System.Text;


namespace NfcAimeReaderDLL;

public class WebSocketServers
{
    static Card card;
    public static void RunWebSocketServer(Card cards)
    {
        card = cards;
        var listenerAddress = Config.ServerAddress;
        var port = Config.ServerPort;
        var password = Config.Password;
        var server = new WebSocketServer("ws://" + listenerAddress + ":" + port);
        server.RestartAfterListenError = true;
        server.Start(socket =>
        {
            socket.OnOpen = () => Console.WriteLine("Clinet connected! Address:" + socket.ConnectionInfo.ClientIpAddress + ":" + socket.ConnectionInfo.ClientPort);
            socket.OnClose = () => Console.WriteLine("The connection had been lost!");
            socket.OnError = e => Console.WriteLine("Error: " + e);
            socket.OnBinary = data =>
            {
                //处理非明文数据
                byte[] message;
                if (password == "")
                {
                    message = data.ToArray();
                }else 
                {
                    //解密数据
                    try
                    {
                        message = RC4Decryption.DecryptRC4(data.ToArray(), Encoding.UTF8.GetBytes(password));
                    }
                    catch (Exception ex)
                    {
                        Console.WriteLine("Packet decryption error: " + ex);
                        message = [0x00];
                    }
                    
                };
                string json = Encoding.UTF8.GetString(message);
                Console.WriteLine("Received Binary message: " + json);
                try
                {
                    WebSocketPacketHandle(json);
                }
                catch (JsonReaderException)
                {
                    Console.WriteLine("Received message is not in JSON format.");
                }
               
            };
            socket.OnMessage = message =>
            {
                //处理明文数据
                Console.WriteLine("Received message: " + message);
                try
                {
                    WebSocketPacketHandle(message);
                }
                catch (JsonReaderException)
                {
                    Console.WriteLine("Received message is not in JSON format.");
                }
            };
        });
    }

    public static void WebSocketPacketHandle(string message)
    {
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