package net.sofigo.screenmirroring

import android.app.Activity
import android.graphics.Color
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustAdRevenue
import com.adjust.sdk.AdjustConfig
import com.applovin.mediation.*
import com.applovin.mediation.ads.MaxAdView
import com.applovin.mediation.ads.MaxInterstitialAd
import com.applovin.mediation.ads.MaxRewardedAd
import com.applovin.mediation.nativeAds.MaxNativeAd
import com.applovin.mediation.nativeAds.MaxNativeAdListener
import com.applovin.mediation.nativeAds.MaxNativeAdLoader
import com.applovin.mediation.nativeAds.MaxNativeAdView
import com.applovin.mediation.nativeAds.MaxNativeAdViewBinder
import com.applovin.sdk.AppLovinSdkUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

object ApplovinHepler {

  private lateinit var maxInterstitialAd: MaxInterstitialAd
  private lateinit var rewardedAd: MaxRewardedAd
  private var maxRevenueAd = MaxAdRevenueListener { ad -> //    Log.d("===Interstitial event", "")

    val adjustAdRevenue = AdjustAdRevenue(AdjustConfig.AD_REVENUE_APPLOVIN_MAX)
    adjustAdRevenue.setRevenue(ad?.revenue, "USD")
    adjustAdRevenue.setAdRevenueNetwork(ad?.networkName)
    adjustAdRevenue.setAdRevenueUnit(ad?.adUnitId)
    adjustAdRevenue.setAdRevenuePlacement(ad?.placement)

    Adjust.trackAdRevenue(adjustAdRevenue)
  }

  //endregion
  private var retryAttempt = 0.0
  private lateinit var maxAdListener: MaxAdListener
  private lateinit var maxRewardedAdListener: MaxRewardedAdListener
  private lateinit var maxBannerAdListener: MaxAdViewAdListener
  private lateinit var maxNativeAdListener: MaxNativeAdListener
  private lateinit var nativeAdLoader: MaxNativeAdLoader
  private lateinit var nativeAdView: MaxNativeAdView
  private var nativeAd: MaxAd? = null
  private val maxInterAdState = MutableSharedFlow<AdState>()

  init {
    CoroutineScope(Dispatchers.IO).launch {
      maxInterAdState.emit(AdState.NotReady)
    }
  }


  fun loadMaxInterstitial(activity: Activity) {
    maxInterstitialAd = MaxInterstitialAd("c3a25f0b0b4bebef", activity)
    maxAdListener = object : MaxAdListener {
      override fun onAdLoaded(ad: MaxAd?) {
        // Interstitial ad is ready to be shown. interstitialAd.isReady() will now return 'true'.
        Log.d("===Interstitial event", "Loaded")
        // Reset retry attempt
        retryAttempt = 0.0
        if (maxInterstitialAd.isReady) {
          CoroutineScope(Dispatchers.IO).launch {
            maxInterAdState.emit(AdState.Ready)
          }
        }
      }

      override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {
        Log.d("===Interstitial event", "Load Failed ${error?.code}-${error?.message}")
        // Interstitial ad failed to load. We recommend retrying with exponentially higher delays up to a maximum delay (in this case 64 seconds).
        retryAttempt++
        CoroutineScope(Dispatchers.IO).launch {
          maxInterAdState.emit(AdState.NotReady)
        }
        val delayMillis = TimeUnit.SECONDS.toMillis(Math.pow(2.0, Math.min(6.0, retryAttempt)).toLong())
        Handler().postDelayed({ maxInterstitialAd.loadAd() }, delayMillis)
      }

      override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {
        Log.d("===Interstitial event", "Display failed")

        // Interstitial ad failed to display. We recommend loading the next ad.
        maxInterstitialAd.loadAd()
      }

      override fun onAdDisplayed(ad: MaxAd?) {
        Log.d("===Interstitial event", "Displayed")
      }

      override fun onAdClicked(ad: MaxAd?) {
        Log.d("===Interstitial event", "Clicked")
      }

      override fun onAdHidden(ad: MaxAd?) {
        Log.d("===Interstitial event", "Hidden")
        // Interstitial Ad is hidden. Pre-load the next ad
        maxInterstitialAd.loadAd()
      }
    }
    maxInterstitialAd.setListener(maxAdListener)
//    interstitialAd.setRevenueListener(this)

    // Load the first ad.
    maxInterstitialAd.loadAd()
    CoroutineScope(Dispatchers.IO).launch {
      maxInterAdState.emit(AdState.Loading)
    }
  }

  fun showMaxInterstitial(activity: Activity) {
//    val jog = CoroutineScope(Dispatchers.IO).launch {
//      maxInterAdState.collect {adState  ->
//        when(adState) {
//        is AdState.Ready -> showMaxInterstitial(activity)
//        is AdState.NotReady -> loadMaxInterstitial(activity)
//        else -> {}
//        }
//  }
    if (maxInterstitialAd.isReady) {
      maxInterstitialAd.showAd()
    }
  }

  fun loadMaxRewarded(activity: Activity) {
    rewardedAd = MaxRewardedAd.getInstance("fa5ccf09afffada3", activity)
    maxRewardedAdListener = object : MaxRewardedAdListener {
      override fun onAdLoaded(ad: MaxAd?) {
        // Rewarded ad is ready to be shown. rewardedAd.isReady() will now return 'true'
        Log.d("===rewarded event", "loaded ${ad?.networkName}")

        // Reset retry attempt
        retryAttempt = 0.0
      }

      override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {
        Log.d("===rewarded event", "load failed")

        // Rewarded ad failed to load. We recommend retrying with exponentially higher delays up to a maximum delay (in this case 64 seconds).

        retryAttempt++
        val delayMillis = TimeUnit.SECONDS.toMillis(Math.pow(2.0, Math.min(6.0, retryAttempt)).toLong())

        Handler().postDelayed({ rewardedAd.loadAd() }, delayMillis)
      }

      override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {
        Log.d("===rewarded event", "display failed")

        // Rewarded ad failed to display. We recommend loading the next ad.
        rewardedAd.loadAd()
      }

      override fun onAdDisplayed(ad: MaxAd?) {
        Log.d("===rewarded event", "displayed")
      }

      override fun onAdClicked(ad: MaxAd?) {
        Log.d("===rewarded event", "")
      }

      override fun onAdHidden(ad: MaxAd?) {
        Log.d("===rewarded event", "hidden")

        // Rewarded ad is hidden. Pre-load the next ad.
        rewardedAd.loadAd()
      }

      override fun onRewardedVideoStarted(ad: MaxAd?) {
        Log.d("===rewarded event", "video started")
      }

      override fun onRewardedVideoCompleted(ad: MaxAd?) {
        Log.d("===rewarded event", "video completed")
      }

      override fun onUserRewarded(ad: MaxAd?, reward: MaxReward?) {
        // Rewarded ad was displayed and user should receive the reward.
        Log.d("===rewarded event", "rewarded")
      }
    }
    rewardedAd.setListener(maxRewardedAdListener)
    rewardedAd.setRevenueListener(maxRevenueAd)

    rewardedAd.loadAd()
  }

  fun showMaxRewarded() {
    if (rewardedAd.isReady) {
      rewardedAd.showAd()
    }
  }

  fun showMaxBanner(activity: Activity): MaxAdView {
    val adView = MaxAdView("c870a6d5e8a0beac", activity)
    maxBannerAdListener = object : MaxAdViewAdListener {
      //region MAX Ad Listener

      override fun onAdLoaded(ad: MaxAd?) {
        Log.d("===banner event", "loaded ${ad?.networkName}")
      }

      override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {
        Log.d("===banner event", "load failed ${error?.code}-${error?.message}")
      }

      override fun onAdHidden(ad: MaxAd?) {
        Log.d("===banner event", "")
      }

      override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {
        Log.d("===banner event", "display failed ${error?.code}-${error?.message}")
      }

      override fun onAdDisplayed(ad: MaxAd?) {
        Log.d("===banner event", "displayed")
      }

      override fun onAdClicked(ad: MaxAd?) {
        Log.d("===banner event", "clicked")
      }

      override fun onAdExpanded(ad: MaxAd?) {
        Log.d("===banner event", "expaned")
      }

      override fun onAdCollapsed(ad: MaxAd?) {
        Log.d("===banner event", "collapsed")
      }
    }
    adView.setListener(maxBannerAdListener)
    adView.setRevenueListener(maxRevenueAd)
    adView.loadAd()
    return adView
  }

  fun showMaxNative(activity: Activity, viewContainer: FrameLayout, layoutId: Int = R.layout.native_custom_ad_view) {
    val binder: MaxNativeAdViewBinder = MaxNativeAdViewBinder.Builder(layoutId)
        .setTitleTextViewId(R.id.title_text_view)
        .setBodyTextViewId(R.id.body_text_view)
        .setAdvertiserTextViewId(R.id.advertiser_text_view)
        .setIconImageViewId(R.id.icon_image_view)
        .setMediaContentViewGroupId(R.id.media_view_container)
        .setOptionsContentViewGroupId(R.id.options_view)
        .setCallToActionButtonId(R.id.cta_button)
        .build()
    nativeAdView = MaxNativeAdView(binder, activity)

    nativeAdLoader = MaxNativeAdLoader("e36e249773a784cb", activity)
    maxNativeAdListener = object : MaxNativeAdListener() {
      override fun onNativeAdLoaded(nativeAdView: MaxNativeAdView?, ad: MaxAd) {
        Log.d("===native event", "")

        // Cleanup any pre-existing native ad to prevent memory leaks.
        if (nativeAd != null) {
          nativeAdLoader.destroy(nativeAd)
        }

        // Save ad for cleanup.
        nativeAd = ad

        // Add ad view to view.
        viewContainer.removeAllViews()
        viewContainer.addView(nativeAdView)
      }

      override fun onNativeAdLoadFailed(adUnitId: String, error: MaxError) {
        Log.d("===native event", "")
      }

      override fun onNativeAdClicked(ad: MaxAd) {
        Log.d("===native event", "")
      }

      override fun onNativeAdExpired(nativeAd: MaxAd?) {
        Log.d("===native event", "")
      }
    }
    nativeAdLoader.setNativeAdListener(maxNativeAdListener)
    nativeAdLoader.setRevenueListener(maxRevenueAd)
    nativeAdLoader.loadAd(nativeAdView)
  }

  fun destroyMaxNative() {
    if(::nativeAdLoader.isInitialized) {
      if (nativeAd != null) nativeAdLoader.destroy(nativeAd)
      nativeAdLoader.destroy()
    }
  }

}

sealed class AdState {
  object Ready : AdState()
  object NotReady : AdState()
  object Loading : AdState()
}