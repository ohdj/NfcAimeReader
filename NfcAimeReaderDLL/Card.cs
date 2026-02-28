namespace NfcAimeReaderDLL;

public class Card {
    public byte[] CardIDm;

    public byte[] CardAccessCode;

    public bool IsIDmMode = false;
    //过期时间
    public long ExpiredTime;
    public Card(String IDm ,String AccessCode) {
        CardIDm = IDmHandle(IDm);
        CardAccessCode = AccessCodeHandle(AccessCode);
    }
    public void SetCardIdm(String IDm) {
        CardIDm = IDmHandle(IDm);
        IsIDmMode = true;
    }

    public void SetCardAccessCode(String accessCode) {
        CardAccessCode = AccessCodeHandle(accessCode);
        IsIDmMode = false;
    }

    //处理AccessCode String->byte[]
    public byte[] AccessCodeHandle(String accessCode) {
        ExpiredTime = Environment.TickCount64 + 5000;
        return (!string.IsNullOrWhiteSpace(accessCode) && accessCode.Length == 20 && accessCode.All(char.IsAsciiHexDigit)
            ? Convert.FromHexString(accessCode.PadLeft(20, '0'))
            : null);
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