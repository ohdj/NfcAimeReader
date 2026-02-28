using System.Runtime.InteropServices;
using Windows.Win32;
using Fleck;
using Newtonsoft.Json.Linq;
using Newtonsoft.Json;

namespace NfcAimeReaderDLL;

public static class DllMain
{
    public static Card card;
    internal static long LastPollTime = -1;
    public static WebSocketServer Server;

    static DllMain()
    {
        PInvoke.AllocConsole();
        card = new Card("" ,"") ;
        Server = WebSocketServers.GetWebSocketServer();
    }

    //返回API版本
    [UnmanagedCallersOnly(EntryPoint = "aime_io_get_api_version")]
    public static ushort GetApiVersion() => 0x0100;

    [UnmanagedCallersOnly(EntryPoint = "aime_io_init")]
    public static int Init()
    {
        Console.WriteLine("Initializing WebSocket server...");
        Server.Start(socket =>
        {
            socket.OnOpen = () =>
            {
                Console.WriteLine("Client connected! Address:" + socket.ConnectionInfo.ClientIpAddress + ":" + socket.ConnectionInfo.ClientPort);
                WebSocketServers.ActiveConnections.Add(socket);
            };
            socket.OnClose = () =>
            {
                Console.WriteLine("Client disconnected: " + socket.ConnectionInfo.ClientIpAddress + ":" + socket.ConnectionInfo.ClientPort);
                WebSocketServers.ActiveConnections.Remove(socket);
            };
            socket.OnError = e => Console.WriteLine("Error: " + e);
            socket.OnMessage = async message =>
            {
                Console.WriteLine("Received message: " + message);
                try
                {
                    //尝试解析为JSON
                    JObject jsonObj = JObject.Parse(message);
                    string? module = jsonObj["module"]?.ToString();
                    string? function = jsonObj["function"]?.ToString();

                    // Check if this is a card insert request
                    if (module == "card" && function == "insert")
                    {
                        var jsonParams = jsonObj["params"]?.ToString();
                        if (!string.IsNullOrEmpty(jsonParams))
                        {
                            if (jsonParams.Length == 20) {
                                Console.WriteLine("AccessCode: " + jsonParams);
                                card.SetCardAccessCode(jsonParams);
                            }
                            else {
                                Console.WriteLine("IDm: " + jsonParams);
                                card.SetCardIdm(jsonParams);
                            }
                            // Send success response
                            await socket.Send(JsonConvert.SerializeObject(new { status = true }));
                        }
                        else
                        {
                            // Send error response if params is missing
                            await socket.Send(JsonConvert.SerializeObject(new { status = false, error = "Missing card ID" }));
                        }
                    }
                    else
                    {
                        // Send error response for unknown command
                        await socket.Send(JsonConvert.SerializeObject(new { status = false, error = "Unknown command" }));
                    }
                }
                catch (JsonReaderException)
                {
                    Console.WriteLine("Received message is not in JSON format.");
                    await socket.Send(JsonConvert.SerializeObject(new { status = false, error = "Invalid JSON format" }));
                }
                catch (Exception ex)
                {
                    Console.WriteLine($"Error processing message: {ex.Message}");
                    await socket.Send(JsonConvert.SerializeObject(new { status = false, error = "Internal error" }));
                }
            };
        });

        return 0;
    }

    //卡轮询
    [UnmanagedCallersOnly(EntryPoint = "aime_io_nfc_poll")]
    public static int NfcPoll(byte unitNo)
    {
        Volatile.Write(ref LastPollTime, Environment.TickCount64);
        //Console.WriteLine("Polling...");
        // 检查空格键是否按下
        var enterPressed = (PInvoke.GetAsyncKeyState(0x0D) & 0x8000) != 0;
        if (!enterPressed)
        {
            return 0;
        }
        //Console.WriteLine("回车刷卡暂不支持!");
        return 0;
    }

    //获取Aime ID
    [UnmanagedCallersOnly(EntryPoint = "aime_io_nfc_get_aime_id")]
    public static int GetAimeId(byte unitNo, IntPtr luid, nint luidSize)
    {
        //Console.WriteLine("Getting Aime ID...");
        if (unitNo != 0)
        {
            return 1;
        }
        if (card == null || card.IsCardExpired() || card.IsIDmMode)
        {
            return 1;
        }
        //将卡号复制到缓存区以传递给游戏
        Marshal.Copy(card.CardAccessCode, 0, luid, (int)luidSize);
        Console.WriteLine("Successfully copied card AccessCode to buffer.");

        // Notify connected clients that the card was read
        WebSocketServers.BroadcastMessage(new
        {
            event_type = "card_read",
            card_id = card.CardAccessCode
        });
        return 0;
    }

    //获取FeliCa ID
    [UnmanagedCallersOnly(EntryPoint = "aime_io_nfc_get_felica_id")]
    public static unsafe int GetFelicaId(byte unitNo, ulong* idm)
    {
        if (card == null || card.IsCardExpired() || card.IsIDmMode == false)
        {
            return 1;
        }

        ulong idmValue = 0;
        for (var i = 0; i < 8; i++)
        {
            idmValue = (idmValue << 8) | card.CardIDm[i];
        }

        *idm = idmValue;
        Console.WriteLine("Card read successful!");

        // Convert the byte array to hex string for notification
        string hexString = BitConverter.ToString(card.CardIDm).Replace("-", "");

        // Notify connected clients that the card was read
        WebSocketServers.BroadcastMessage(new
        {
            event_type = "card_read",
            card_id = hexString
        });

        return 0;
    }

    //设置LED颜色
    [UnmanagedCallersOnly(EntryPoint = "aime_io_led_set_color")]
    public static void SetLedColour(byte unitNo, byte r, byte g, byte b)
    {
    }
}