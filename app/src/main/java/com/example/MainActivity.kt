package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppUi
import com.example.ui.AppViewModel
import com.example.ui.theme.MyApplicationTheme
import com.google.android.gms.ads.MobileAds
import kotlin.concurrent.thread

class MainActivity : ComponentActivity() {
    private val micPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* result handled lazily by features */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // AdMob init is heavy; run off the main thread as Google recommends
        thread { MobileAds.initialize(this@MainActivity) }

        // The mic is needed for Quran recitation analysis and voice input;
        // without this runtime request both features fail silently.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

        setContent {
            val viewModel: AppViewModel = viewModel()
            val themeMode by viewModel.appTheme.collectAsState()
            MyApplicationTheme(themeMode = themeMode) {
                AppUi(viewModel = viewModel)
            }
        }
    }
}
