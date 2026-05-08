package com.jaymin.smartconnect.core.data.nfc

import android.app.Activity
import android.content.Context
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.NfcManager
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import com.jaymin.smartconnect.core.common.util.Constants
import com.jaymin.smartconnect.core.common.util.Resource
import com.jaymin.smartconnect.core.domain.model.NfcPayload
import com.jaymin.smartconnect.core.domain.model.PayloadType
import com.jaymin.smartconnect.core.domain.repository.NfcRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NfcController @Inject constructor(
    @ApplicationContext private val context: Context
) : NfcRepository {

    private val nfcManager = context.getSystemService(Context.NFC_SERVICE) as? NfcManager
    private val nfcAdapter: NfcAdapter? = nfcManager?.defaultAdapter

    override fun isNfcAvailable(): Boolean = nfcAdapter != null
    override fun isNfcEnabled(): Boolean = nfcAdapter?.isEnabled == true

    override fun readTag(): Flow<Resource<NfcPayload>> = callbackFlow {
        trySend(Resource.Loading)

        if (!isNfcEnabled()) {
            trySend(Resource.Error("NFC is disabled. Please enable it in settings."))
            close()
            return@callbackFlow
        }

        // NFC reading is handled via Activity intent in the actual app.
        // This flow emits when a tag is detected via the Activity's onNewIntent.
        awaitClose { }
    }

    override suspend fun writeTag(payload: NfcPayload): Resource<Unit> {
        // Writing is triggered when a tag is physically tapped.
        // The actual write happens in writeToTag() called from the Activity.
        return Resource.Error("Tap an NFC tag to write data")
    }

    override suspend fun shareViaNfc(payload: NfcPayload): Resource<Unit> {
        if (!isNfcEnabled()) return Resource.Error("NFC is disabled")
        // Android Beam is deprecated; use NFC tag writing instead
        return Resource.Success(Unit)
    }

    /**
     * Called from the Activity when an NFC tag is detected.
     * Parses the NDEF message into a domain NfcPayload.
     */
    fun parseTag(tag: Tag): Resource<NfcPayload> {
        return try {
            val ndef = Ndef.get(tag) ?: return Resource.Error("Tag is not NDEF formatted")
            ndef.connect()
            val message = ndef.ndefMessage
            ndef.close()

            if (message == null || message.records.isEmpty()) {
                return Resource.Error("Empty NFC tag")
            }

            val record = message.records[0]
            val content = String(record.payload).trimStart { it.code < 32 }
            val type = when {
                record.tnf == NdefRecord.TNF_WELL_KNOWN &&
                    record.type.contentEquals(NdefRecord.RTD_URI) -> PayloadType.URL
                record.tnf == NdefRecord.TNF_WELL_KNOWN &&
                    record.type.contentEquals(NdefRecord.RTD_TEXT) -> PayloadType.TEXT
                else -> PayloadType.CUSTOM_DATA
            }

            Resource.Success(
                NfcPayload(
                    type = type,
                    content = content,
                    senderDevice = bytesToHex(tag.id)
                )
            )
        } catch (e: Exception) {
            Resource.Error("Failed to read NFC tag: ${e.message}")
        }
    }

    /**
     * Writes an NfcPayload to a physical NFC tag.
     */
    fun writeToTag(tag: Tag, payload: NfcPayload): Resource<Unit> {
        return try {
            val record = when (payload.type) {
                PayloadType.URL -> NdefRecord.createUri(payload.content)
                PayloadType.TEXT -> NdefRecord.createTextRecord("en", payload.content)
                PayloadType.CONTACT -> NdefRecord.createMime(
                    "text/vcard",
                    payload.content.toByteArray()
                )
                PayloadType.WIFI_CREDENTIALS -> NdefRecord.createMime(
                    Constants.NFC_MIME_TYPE,
                    payload.content.toByteArray()
                )
                PayloadType.CUSTOM_DATA -> NdefRecord.createMime(
                    Constants.NFC_MIME_TYPE,
                    payload.content.toByteArray()
                )
            }

            val message = NdefMessage(arrayOf(record))

            val ndef = Ndef.get(tag)
            if (ndef != null) {
                ndef.connect()
                if (!ndef.isWritable) {
                    ndef.close()
                    return Resource.Error("Tag is read-only")
                }
                if (ndef.maxSize < message.toByteArray().size) {
                    ndef.close()
                    return Resource.Error("Tag capacity too small")
                }
                ndef.writeNdefMessage(message)
                ndef.close()
                Resource.Success(Unit)
            } else {
                val formatable = NdefFormatable.get(tag)
                    ?: return Resource.Error("Tag is not NDEF formatable")
                formatable.connect()
                formatable.format(message)
                formatable.close()
                Resource.Success(Unit)
            }
        } catch (e: Exception) {
            Resource.Error("Write failed: ${e.message}")
        }
    }

    private fun bytesToHex(bytes: ByteArray): String =
        bytes.joinToString(":") { "%02X".format(it) }
}
