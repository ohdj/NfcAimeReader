import android.annotation.SuppressLint
import android.nfc.Tag
import android.nfc.tech.NfcF
import android.util.Log
import org.ohdj.nfcaimereader.libs.INFCHandler
import org.ohdj.nfcaimereader.libs.FeliCaDecryptor
import java.io.IOException
import java.nio.ByteBuffer
import kotlin.collections.joinToString

class FeliCaHandler(private val nfcF: NfcF, private val compatibilityMode: Boolean): INFCHandler {
    private val TAG = "FeliCaHandler";
    private fun Short.reverseBytes(): Short {
        val i = this.toInt()
        val hi = (i shr 8) and 0xFF
        val lo = i and 0xFF
        return ((lo shl 8) or hi).toShort()
    }
    private fun readSPAD0(): ByteArray? {
        val idm = nfcF.tag.id
        val serviceCode = 0x000B // Scratch Pad service code
        val blockNumber = 0

        // FeliCa Read Without Encryption command structure:
        // [0] Length
        // [1] Command code (0x06)
        // [2..9] IDm
        // [10] Service count
        // [11..12] Service code (little endian)
        // [13] Block count
        // [14..] Block list elements (2 bytes each)

        val buffer = ByteBuffer.allocate(16)
        buffer.put(0x00) // Placeholder for length
        buffer.put(0x06) // Read Without Encryption
        buffer.put(idm)
        buffer.put(0x01) // Number of service codes
        buffer.putShort(serviceCode.toShort().reverseBytes()) // Little endian
        buffer.put(0x01) // Number of blocks
        buffer.put(0x80.toByte()) // Block list element: 0x80 = direct block number
        buffer.put(blockNumber.toByte())

        val packet = buffer.array()
        packet[0] = packet.size.toByte()

        val response = nfcF.transceive(packet)
        // response[1] == 0x07 is normal response
        return if (response.isNotEmpty() && response[1] == 0x07.toByte()) {
            // Data starts after length(1) + response code(1) + IDm(8) + status flag1(1) + status flag2(1) + block count(1)
            // = 13 bytes
            response.copyOfRange(13, response.size)
        } else {
            null
        }
    }

    private fun sendPolling(systemCode: Int = 0x88B4): ByteArray? {
        // FeliCa Polling packet:
        // [0] length
        // [1] command code (0x00)
        // [2..3] system code (big endian)
        // [4] request code (0x01 = system code request)
        // [5] time slot (0x0F = max)

        val buffer = ByteBuffer.allocate(6)
        buffer.put(0x06) // length
        buffer.put(0x00) // polling command
        buffer.putShort(systemCode.toShort()) // big endian for system code
        buffer.put(0x01) // request code
        buffer.put(0x0F) // time slot

        val packet = buffer.array()
        return try {
            nfcF.transceive(packet)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    @SuppressLint("DefaultLocale")
    override fun getAccessCode(): String? {
        if (compatibilityMode) return nfcF.tag.id.joinToString (""){ String.format("%02X", it) };
        try {
            nfcF.connect()
            sendPolling()
            val response = readSPAD0()
            if (response != null) {
                val SPAD0 = response.joinToString("") { String.format("%02X", it) }
                Log.d(TAG, "Got SPAD0: $SPAD0")
                val decryptedAccessCode = FeliCaDecryptor().decrypt(response).joinToString("") { String.format("%02X", it) }.takeLast(20)
                Log.d(TAG, "-> Access Code = $decryptedAccessCode")
                return decryptedAccessCode
            } else {
                Log.w(TAG, "Failed to read SPAD0")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try { nfcF.close() } catch (e: IOException) { }
        }
        return null
    }
}
