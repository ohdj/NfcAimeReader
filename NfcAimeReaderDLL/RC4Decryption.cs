using Org.BouncyCastle.Crypto.Engines;
using Org.BouncyCastle.Crypto.Parameters;
using System;

namespace NfcAimeReaderDLL;

public class RC4Decryption
{
    public static byte[] DecryptRC4(byte[] encryptedData, byte[] key)
    {
        var rc4 = new RC4Engine();
        rc4.Init(false, new KeyParameter(key));
        var decryptedData = new byte[encryptedData.Length];
        rc4.ProcessBytes(encryptedData, 0, encryptedData.Length, decryptedData, 0);
        return decryptedData;
    }
}
