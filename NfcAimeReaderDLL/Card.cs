namespace NfcAimeReaderDLL;

public class Card
{
    public byte[] CardIDm { get; private set; }
    public long ExpiredTime { get; private set; }

    // Card 过期时间（毫秒）
    private const int CardExpirationTimeMs = 5000;

    public Card(String IDm)
    {
        CardIDm = IDmHandle(IDm);
    }

    public void SetCardIdm(String IDm)
    {
        CardIDm = IDmHandle(IDm);
        // 每次设置卡号时重置过期时间
        ResetExpiration();
    }

    //处理IDm String->byte[]
    public byte[] IDmHandle(String idmHex)
    {
        ResetExpiration();

        // 验证卡号格式
        if (string.IsNullOrWhiteSpace(idmHex) || idmHex.Length > 16 || !idmHex.All(char.IsAsciiHexDigit))
        {
            Console.WriteLine($"Invalid card ID format: {idmHex}");
            return null;
        }

        // 将16进制字符串转换为字节数组
        var paddedHex = idmHex.PadLeft(16, '0');
        var bytes = Convert.FromHexString(paddedHex);

        Console.WriteLine($"Card registered with ID: {paddedHex}");
        return bytes;
    }

    // 重置卡的过期时间
    private void ResetExpiration()
    {
        ExpiredTime = Environment.TickCount64 + CardExpirationTimeMs;
    }

    // 返回卡是否过期
    public bool IsCardExpired()
    {
        var currentTime = Environment.TickCount64;
        var isExpired = currentTime >= ExpiredTime;

        if (isExpired)
        {
            Console.WriteLine("Card has expired");
        }

        return isExpired;
    }
}