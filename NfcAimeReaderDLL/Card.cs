namespace NfcAimeReaderDLL;

public class Card {
    public byte[] CardIDm;
    //过期时间
    public long ExpiredTime;
    public Card(String IDm) {
        CardIDm = IDmHandle(IDm);
    }
    public void SetCardIdm(String IDm) {
        CardIDm = IDmHandle(IDm);
    }

    //处理IDm String->byte[]
    public byte[] IDmHandle(String idmHex)
    {
        ExpiredTime = Environment.TickCount64 + 5000;
        return (!string.IsNullOrWhiteSpace(idmHex) && idmHex.Length <= 16 && idmHex.All(char.IsAsciiHexDigit)
            ? Convert.FromHexString(idmHex.PadLeft(16, '0'))
            : null);
    }
    //返回卡是否过期
    public bool IsCardExpired() {
        //Console.WriteLine("ExpiredTime:" + ExpiredTime);
        //Console.WriteLine("Now Time:" + Environment.TickCount64);
        //Console.WriteLine("IsExpired:" + (Environment.TickCount64 >= ExpiredTime));
        return  Environment.TickCount64 >= ExpiredTime;
    }

}