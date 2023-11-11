package com.example.loveanddate

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.bumptech.glide.Glide

class DislikeActivity : AppCompatActivity() {

    private val TAG = "DislikeActivity"
    private val ACTIVITY_NUM = 1
    private val mContext: Context = this
    private lateinit var dislike: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dislike)

        dislike = findViewById(R.id.dislike)

        val intent = intent
        val profileUrl = intent.getStringExtra("url")
        when (profileUrl) {
            "default" -> Glide.with(mContext).load(R.drawable.cross).into(dislike)  // cross değişecek
            else -> Glide.with(mContext).load(profileUrl).into(dislike)
        }

        Thread {
            try {
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            val mainIntent = Intent(this@DislikeActivity, MainActivity::class.java)
            startActivity(mainIntent)
        }.start()
    }
}