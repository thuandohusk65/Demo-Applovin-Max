package net.sofigo.screenmirroring

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.applovin.mediation.ads.MaxAdView
import com.applovin.sdk.AppLovinSdkUtils

class MainActivity : AppCompatActivity() {
  private lateinit var txtInterstitial: TextView
  private lateinit var txtRewarded: TextView
  private lateinit var maxAdBannerView: MaxAdView
  private lateinit var viewContainerBanner: FrameLayout
  private lateinit var viewContainerNative: FrameLayout

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    ApplovinHepler.loadMaxInterstitial(this)
    ApplovinHepler.loadMaxRewarded(this)
    setupView()
    setupAction()
  }

  private fun setupView() {
    txtInterstitial = findViewById(R.id.txtApplovinMaxInterstitial)
    txtRewarded = findViewById(R.id.txtApplovinMaxRewarded)
    viewContainerBanner = findViewById(R.id.viewContainerBanner)
    viewContainerNative = findViewById(R.id.viewContainerNative)
    setupMaxBannerAd()
    setupMaxNativeAd()


  }

  private fun setupMaxNativeAd() {
    ApplovinHepler.showMaxNative(this, viewContainerBanner)
  }

  private fun setupMaxBannerAd() {
    val isTablet = AppLovinSdkUtils.isTablet(this)
    val heightPx = AppLovinSdkUtils.dpToPx(this, if (isTablet) 90 else 50)
    maxAdBannerView = ApplovinHepler.showMaxBanner(this)
    maxAdBannerView.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, heightPx)
    maxAdBannerView.setBackgroundColor(Color.BLACK)
    viewContainerBanner.addView(maxAdBannerView)
    maxAdBannerView.loadAd()
  }

  private fun setupAction() {
    txtInterstitial.setOnClickListener {
      ApplovinHepler.showMaxInterstitial(this)
    }
    txtRewarded.setOnClickListener {
      ApplovinHepler.showMaxRewarded()
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    maxAdBannerView.destroy()
    ApplovinHepler.destroyMaxNative()
  }
}