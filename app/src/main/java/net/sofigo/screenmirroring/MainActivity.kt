package net.sofigo.screenmirroring

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView


class MainActivity : AppCompatActivity() {
  private lateinit var txtInterstitial: TextView
  private lateinit var txtRewarded: TextView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    ApplovinHepler.loadMaxInterstitial(this)
    ApplovinHepler.loadMaxRewarded(this)
    txtInterstitial = findViewById(R.id.txtApplovinMaxInterstitial)
    txtRewarded = findViewById(R.id.txtApplovinMaxRewarded)
    txtInterstitial.setOnClickListener {
      ApplovinHepler.showMaxInterstitial(this)
    }
    txtRewarded.setOnClickListener {
      ApplovinHepler.showMaxRewarded()
    }
  }
}