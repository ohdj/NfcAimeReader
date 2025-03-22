namespace NfcAimeReaderDLL;

public class Card {
    public static Card card;
    public byte[] CardIDm;
    public byte[] CardAccessCode;
    public Boolean IsUsePhysicalCard;
    //过期时间
    public long ExpiredTime;
    public Card(String IDm) {
        CardIDm = IDmHandle(IDm);
        card = this;
    }
    
    public void SetCardIdm(String IDm) {
        CardIDm = IDmHandle(IDm);
        IsUsePhysicalCard = true;
        ExpiredTime = Environment.TickCount64 + 5000;
    }

    public void SetCardAccessCode(String accessCode)
    {
        CardAccessCode = AccessCodeHandle(accessCode);
        IsUsePhysicalCard = false;
        ExpiredTime = Environment.TickCount64 + 5000;
    }

    //处理IDm String->byte[]
    public byte[] IDmHandle(String idmHex)
    {
        return (!string.IsNullOrWhiteSpace(idmHex) && idmHex.Length <= 16 && idmHex.All(char.IsAsciiHexDigit)
            ? Convert.FromHexString(idmHex.PadLeft(16, '0'))
            : null);
    }

    //处理AccessCode String->byte[]
    public byte[] AccessCodeHandle(String accessCode)
    {
        byte[] result = new byte[10];
        for (int i = 0; i < 10; i++) 
        {
            result[i] = Convert.ToByte(accessCode.Substring(i * 2, 2), 16);
        }
        return result;
    }
    //返回卡是否过期
    public bool IsCardExpired() {
        return  Environment.TickCount64 >= ExpiredTime;
    }

}