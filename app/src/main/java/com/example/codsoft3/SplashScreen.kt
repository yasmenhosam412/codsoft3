package com.example.codsoft3

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AccelerateInterpolator
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.codsoft3.databinding.ActivitySplashScreenBinding

class SplashScreen : AppCompatActivity() {

    private lateinit var binding: ActivitySplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }




        animateImageView()
    }

    private fun animateImageView() {
        binding.imageView2.translationY = 2000f

        ObjectAnimator.ofFloat(binding.imageView2, "translationY", 0f).apply {
            duration = 1500
            interpolator = AccelerateInterpolator()
            start()
        }
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity2::class.java))
            finish()
        }, 2000)
    }
}
