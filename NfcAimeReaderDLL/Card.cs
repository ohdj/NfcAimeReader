namespace NfcAimeReaderDLL;

public class Card
{
    public byte[] CardIDm;
    public string DecimalCardNumber; // Add this field to store the original decimal number
    public long ExpiredTime;

    public Card(string cardNumber = "")
    {
        SetCardNumber(cardNumber);
    }

    public void SetCardNumber(string cardNumber)
    {
        DecimalCardNumber = cardNumber;
        CardIDm = DecimalToIDm(cardNumber);
        ExpiredTime = Environment.TickCount64 + 5000;
    }

    // Convert 20-digit decimal to byte array for internal use
    private byte[] DecimalToIDm(string decimalNumber)
    {
        // Return null for invalid input
        if (string.IsNullOrWhiteSpace(decimalNumber) || !decimalNumber.All(char.IsDigit) || decimalNumber.Length > 20)
            return null;

        // Pad to 20 digits and convert to bytes (we'll use 8 bytes as before)
        string paddedNumber = decimalNumber.PadLeft(20, '0');
        // Convert to hex representation to maintain compatibility with existing code
        // This converts the decimal string to a ulong then to hex
        if (ulong.TryParse(paddedNumber, out ulong cardValue))
        {
            string hexValue = cardValue.ToString("X16"); // Converts to 16-char hex
            return Convert.FromHexString(hexValue);
        }
        return null;
    }

    // Returns whether the card is expired
    public bool IsCardExpired()
    {
        return Environment.TickCount64 >= ExpiredTime;
    }
}