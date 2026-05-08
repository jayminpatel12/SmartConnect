package com.jaymin.smartconnect

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.jaymin.smartconnect.core.data.nfc.NfcController
import com.jaymin.smartconnect.ui.navigation.AppNavigation
import com.jaymin.smartconnect.ui.theme.SmartConnectTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var nfcController: NfcController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartConnectTheme {
                AppNavigation()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Enable foreground dispatch for NFC
        val adapter = NfcAdapter.getDefaultAdapter(this) ?: return
        val pendingIntent = android.app.PendingIntent.getActivity(
            this, 0,
            android.content.Intent(this, javaClass).addFlags(android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP),
            android.app.PendingIntent.FLAG_MUTABLE
        )
        adapter.enableForegroundDispatch(this, pendingIntent, null, null)
    }

    override fun onPause() {
        super.onPause()
        NfcAdapter.getDefaultAdapter(this)?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        if (NfcAdapter.ACTION_TAG_DISCOVERED == intent.action ||
            NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action ||
            NfcAdapter.ACTION_TECH_DISCOVERED == intent.action) {
            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            tag?.let {
                // Parse the NFC tag via the controller
                nfcController.parseTag(it)
            }
        }
    }
}
