package net.sofigo.screenmirroring

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.applovin.mediation.ads.MaxAdView
import com.applovin.sdk.AppLovinSdkUtils
import kotlin.math.max


class MainActivity : AppCompatActivity() {
  private lateinit var txtInterstitial: TextView
  private lateinit var txtRewarded: TextView
  private lateinit var maxAdView: MaxAdView

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
    setupMaxBannerAd()


  }

  private fun setupMaxBannerAd() {
    val isTablet = AppLovinSdkUtils.isTablet(this)
    val heightPx = AppLovinSdkUtils.dpToPx(this, if (isTablet) 90 else 50)
    maxAdView = ApplovinHepler.loadMaxBanner(this)
    maxAdView.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, heightPx)
    maxAdView.setBackgroundColor(Color.BLACK)
    maxAdView.id = R.id.adViewBanner
    val mainConstraintLayout: ConstraintLayout = findViewById(R.id.mainConstraintLayout)
    mainConstraintLayout.addView(maxAdView)
    val constraintSet = ConstraintSet()
    constraintSet.clone(mainConstraintLayout)
    constraintSet.connect(R.id.adViewBanner, ConstraintSet.TOP, R.id.txtApplovinMaxRewarded, ConstraintSet.BOTTOM, 24)
    constraintSet.applyTo(mainConstraintLayout)
    maxAdView.loadAd()
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
    maxAdView.destroy()
  }
}