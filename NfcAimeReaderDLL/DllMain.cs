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
    private static readonly List<IWebSocketConnection> _clients = new List<IWebSocketConnection>();

    static DllMain()
    {
        PInvoke.AllocConsole();
        card = new Card("");
        Server = WebSocketServers.GetWebSocketServer();
    }

    //返回API版本
    [UnmanagedCallersOnly(EntryPoint = "aime_io_get_api_version")]
    public static ushort GetApiVersion() => 0x0100;

    [UnmanagedCallersOnly(EntryPoint = "aime_io_init")]
    public static int Init()
    {
        Console.WriteLine("Initializing NFC Aime Reader...");
        Console.WriteLine($"WebSocket server starting at {Config.ServerAddress}:{Config.ServerPort}");

        Server.Start(socket =>
        {
            socket.OnOpen = () =>
            {
                Console.WriteLine($"Client connected! Address: {socket.ConnectionInfo.ClientIpAddress}:{socket.ConnectionInfo.ClientPort}");
                _clients.Add(socket);
            };

            socket.OnClose = () =>
            {
                Console.WriteLine("Connection closed");
                _clients.Remove(socket);
            };

            socket.OnError = e => Console.WriteLine($"Error: {e}");

            socket.OnMessage = async message =>
            {
                Console.WriteLine($"Received message: {message}");
                try
                {
                    // First check if the message isn't empty
                    if (string.IsNullOrEmpty(message))
                    {
                        Console.WriteLine("Empty message received");
                        await socket.Send(JsonConvert.SerializeObject(new { status = false, message = "Empty message received" }));
                        return;
                    }

                    // Try to parse as JObject which doesn't require a predefined class
                    JObject jsonObj = JObject.Parse(message);
                    Console.WriteLine("Successfully parsed JSON");

                    // Check if required fields exist
                    if (!jsonObj.ContainsKey("module") || !jsonObj.ContainsKey("function") || !jsonObj.ContainsKey("params"))
                    {
                        Console.WriteLine("Missing required fields in JSON");
                        await socket.Send(JsonConvert.SerializeObject(new { status = false, message = "Missing required fields" }));
                        return;
                    }

                    // Extract values explicitly
                    string module = jsonObj["module"].ToString();
                    string function = jsonObj["function"].ToString();
                    string paramValue = jsonObj["params"].ToString();

                    Console.WriteLine($"Module: {module}, Function: {function}, Params: {paramValue}");

                    // Process according to your business logic
                    if (module == "card" && function == "insert")
                    {
                        // Validate card ID format
                        if (!string.IsNullOrEmpty(paramValue) && paramValue.Length <= 16 && paramValue.All(char.IsAsciiHexDigit))
                        {
                            Console.WriteLine($"Setting card IDm: {paramValue}");
                            // Make sure card is not null
                            if (card == null)
                            {
                                card = new Card("");
                                Console.WriteLine("Created new card instance");
                            }

                            card.SetCardIdm(paramValue);
                            Console.WriteLine("Card IDm set successfully");

                            // Send success response
                            await socket.Send(JsonConvert.SerializeObject(new { status = true, message = "Card registered successfully" }));
                        }
                        else
                        {
                            Console.WriteLine($"Invalid card ID format: {paramValue}");
                            await socket.Send(JsonConvert.SerializeObject(new { status = false, message = "Invalid card ID format" }));
                        }
                    }
                    else
                    {
                        Console.WriteLine($"Unsupported module or function: {module}.{function}");
                        await socket.Send(JsonConvert.SerializeObject(new { status = false, message = "Unsupported action" }));
                    }
                }
                catch (JsonReaderException ex)
                {
                    Console.WriteLine($"JSON parsing error: {ex.Message}");
                    await socket.Send(JsonConvert.SerializeObject(new { status = false, message = "Invalid JSON format" }));
                }
                catch (NullReferenceException ex)
                {
                    Console.WriteLine($"Null reference error: {ex.Message}");
                    Console.WriteLine($"Stack trace: {ex.StackTrace}");
                    await socket.Send(JsonConvert.SerializeObject(new { status = false, message = "Internal error: null reference" }));
                }
                catch (Exception ex)
                {
                    Console.WriteLine($"Error processing message: {ex.Message}");
                    Console.WriteLine($"Stack trace: {ex.StackTrace}");
                    await socket.Send(JsonConvert.SerializeObject(new { status = false, message = "Internal error" }));
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

        // 检查空格键是否按下
        var enterPressed = (PInvoke.GetAsyncKeyState(0x0D) & 0x8000) != 0;
        if (!enterPressed)
        {
            return 0;
        }

        return 0;
    }

    //获取Aime ID
    [UnmanagedCallersOnly(EntryPoint = "aime_io_nfc_get_aime_id")]
    public static int GetAimeId(byte unitNo, IntPtr luid, nint luidSize)
    {
        if (unitNo != 0)
        {
            return 1;
        }

        if (card?.IsCardExpired() == true || card?.CardIDm == null)
        {
            return 1;
        }

        //将卡号复制到缓存区以传递给游戏
        Marshal.Copy(card.CardIDm, 0, luid, (int)luidSize);
        Console.WriteLine("Successfully copied card IDm to buffer.");
        return 0;
    }

    //获取FeliCa ID
    [UnmanagedCallersOnly(EntryPoint = "aime_io_nfc_get_felica_id")]
    public static unsafe int GetFelicaId(byte unitNo, ulong* idm)
    {
        if (card == null || card.IsCardExpired())
        {
            return 1;
        }

        ulong idmValue = 0;
        for (var i = 0; i < 8; i++)
        {
            idmValue = (idmValue << 8) | card.CardIDm[i];
        }

        *idm = idmValue;
        Console.WriteLine("Successful!");
        return 0;
    }

    //设置LED颜色
    [UnmanagedCallersOnly(EntryPoint = "aime_io_led_set_color")]
    public static void SetLedColour(byte unitNo, byte r, byte g, byte b)
    {
        // Implementation if needed
    }
}