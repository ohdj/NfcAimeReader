using Windows.Win32;
using Windows.Win32.Foundation;

namespace NfcAimeReaderDLL;

internal static class Config
{
    internal const string IOSection = "aimeio";
    private const string ConfigFileName = @".\segatools.ini";

    static Config()
    {
        ServerAddress = ReadKey(IOSection, "serverAddress", 1024, "0.0.0.0");
        ServerPort = ReadKey(IOSection, "serverPort", 1024, "6071");
    }

    
    public static string ServerPort { get; }
    
    public static string ServerAddress { get; }
    

    //读取键值 From segatools.ini
    internal static unsafe string ReadKey(string section, string key, uint maxLength, string @default = null)
    {
        // +1 for null terminator
        var buffer = stackalloc char[(int)maxLength + 1];
        var bufferStr = new PWSTR(buffer);

        PInvoke.GetPrivateProfileString(section, key, @default ?? string.Empty, bufferStr, maxLength + 1, ConfigFileName);
        return bufferStr.ToString();
    }
}