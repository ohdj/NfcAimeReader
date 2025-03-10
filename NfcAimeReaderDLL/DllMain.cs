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
        card = new Card("") ;
        Server = WebSocketServers.GetWebSocketServer();
    }

    //返回API版本
    [UnmanagedCallersOnly(EntryPoint = "aime_io_get_api_version")]
    public static ushort GetApiVersion() => 0x0100;

    [UnmanagedCallersOnly(EntryPoint = "aime_io_init")]
    public static int Init()
    {
        Console.WriteLine(card.CardIDm);
        Server.Start(socket =>
        {
            socket.OnOpen = () => Console.WriteLine("Clinet connected! Address:" + socket.ConnectionInfo.ClientIpAddress + ":" + socket.ConnectionInfo.ClientPort);
            socket.OnClose = () => Console.WriteLine("The connection had been lost!");
            socket.OnError = e => Console.WriteLine("Error: " + e);
            socket.OnMessage = async message =>
            {
                Console.WriteLine("Received message: " + message);
                try
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
                catch (JsonReaderException)
                {
                    Console.WriteLine("Received message is not in JSON format.");
                }
                await Task.CompletedTask; // Ensure the method is awaited
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
        //Console.WriteLine("回车刷卡暂不支持!");
        return 0;
    }

    //获取Aime ID AcessCode
    [UnmanagedCallersOnly(EntryPoint = "aime_io_nfc_get_aime_id")]
    public static int GetAimeId(byte unitNo, IntPtr luid, nint luidSize)
    {
        if (unitNo != 0)
        {
            return 1;
        }
        if (card == null || card.IsCardExpired() || card.IsUsePhysicalCard == true || card.CardAccessCode == null)
        {
            return 1;
        }
        //将卡号复制到缓存区以传递给游戏
        Marshal.Copy(card.CardAccessCode, 0, luid, (int)luidSize);
        Console.WriteLine("Successfully copied AccessCode to buffer.");
        return 0;
    }

    //获取FeliCa ID
    [UnmanagedCallersOnly(EntryPoint = "aime_io_nfc_get_felica_id")]
    public static unsafe int GetFelicaId(byte unitNo, ulong* idm)
    {
        if (card == null || card.IsCardExpired() || card.IsUsePhysicalCard == false || card.CardIDm == null)
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
    }
}