package net.sofigo.screenmirroring

import android.app.Activity
import android.os.Handler
import android.util.Log
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustAdRevenue
import com.adjust.sdk.AdjustConfig
import com.applovin.mediation.*
import com.applovin.mediation.ads.MaxInterstitialAd
import com.applovin.mediation.ads.MaxRewardedAd
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

object ApplovinHepler {

  private lateinit var maxInterstitialAd: MaxInterstitialAd
  private var maxRevenueAd = MaxAdRevenueListener { ad -> //    Log.d("===Interstitial event", "")

    val adjustAdRevenue = AdjustAdRevenue(AdjustConfig.AD_REVENUE_APPLOVIN_MAX)
    adjustAdRevenue.setRevenue(ad?.revenue, "USD")
    adjustAdRevenue.setAdRevenueNetwork(ad?.networkName)
    adjustAdRevenue.setAdRevenueUnit(ad?.adUnitId)
    adjustAdRevenue.setAdRevenuePlacement(ad?.placement)

    Adjust.trackAdRevenue(adjustAdRevenue)
  }
  private lateinit var rewardedAd: MaxRewardedAd

  //endregion
  private var retryAttempt = 0.0
  private lateinit var maxAdListener: MaxAdListener
  private lateinit var maxRewardedAdListener: MaxRewardedAdListener
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
    rewardedAd = MaxRewardedAd.getInstance("YOUR_AD_UNIT_ID", activity)
    maxRewardedAdListener = object : MaxRewardedAdListener {
      override fun onAdLoaded(ad: MaxAd?) {
        // Rewarded ad is ready to be shown. rewardedAd.isReady() will now return 'true'
        Log.d("===rewarded event", "")

        // Reset retry attempt
        retryAttempt = 0.0
      }

      override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {
        Log.d("===rewarded event", "")

        // Rewarded ad failed to load. We recommend retrying with exponentially higher delays up to a maximum delay (in this case 64 seconds).

        retryAttempt++
        val delayMillis = TimeUnit.SECONDS.toMillis(Math.pow(2.0, Math.min(6.0, retryAttempt)).toLong())

        Handler().postDelayed({ rewardedAd.loadAd() }, delayMillis)
      }

      override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {
        Log.d("===rewarded event", "")

        // Rewarded ad failed to display. We recommend loading the next ad.
        rewardedAd.loadAd()
      }

      override fun onAdDisplayed(ad: MaxAd?) {
        Log.d("===rewarded event", "")
      }

      override fun onAdClicked(ad: MaxAd?) {
        Log.d("===rewarded event", "")
      }

      override fun onAdHidden(ad: MaxAd?) {
        Log.d("===rewarded event", "")

        // Rewarded ad is hidden. Pre-load the next ad.
        rewardedAd.loadAd()
      }

      override fun onRewardedVideoStarted(ad: MaxAd?) {
        Log.d("===rewarded event", "")
      }

      override fun onRewardedVideoCompleted(ad: MaxAd?) {
        Log.d("===rewarded event", "")
      }

      override fun onUserRewarded(ad: MaxAd?, reward: MaxReward?) {
        // Rewarded ad was displayed and user should receive the reward.
        Log.d("===rewarded event", "")
      }
    }
    rewardedAd.setListener(maxRewardedAdListener)
    rewardedAd.setRevenueListener(maxRevenueAd)

    rewardedAd.loadAd()
  }

  fun showMaxRewarded(){
    if(rewardedAd.isReady) {
      rewardedAd.showAd()
    }
  }


}

sealed class AdState {
  object Ready : AdState()
  object NotReady : AdState()
  object Loading : AdState()
}