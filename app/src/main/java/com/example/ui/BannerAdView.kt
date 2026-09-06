package com.example.ui

import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.BuildConfig
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/**
 * Reusable AdMob adaptive banner.
 *
 * In DEBUG builds Google's official test unit is used — clicking your own real
 * ads during development gets the AdMob account permanently banned. The real
 * unit only serves in release builds.
 */
private const val REAL_BANNER_UNIT_ID = "ca-app-pub-2362043522076372/9738581140"
private const val TEST_BANNER_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

@Composable
fun BannerAdView(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val screenWidthDp = LocalConfiguration.current.screenWidthDp

    val adView = remember {
        AdView(context).apply {
            adUnitId = if (BuildConfig.DEBUG) TEST_BANNER_UNIT_ID else REAL_BANNER_UNIT_ID
            setAdSize(
                AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, screenWidthDp)
            )
            try {
                loadAd(AdRequest.Builder().build())
            } catch (e: Exception) {
                Log.e("BannerAdView", "Failed to load banner ad", e)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { adView.destroy() }
    }

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { adView }
    )
}
