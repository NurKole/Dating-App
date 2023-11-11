package com.example.loveanddate

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.bumptech.glide.Glide

class LikeActivity : AppCompatActivity() {

    private val TAG = "LikeActivity"
    private val ACTIVITY_NUM = 1
    private val mContext: Context = this
    private lateinit var like: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_like)


        //setupToolbar()
        like = findViewById(R.id.like)

        val intent = intent
        val profileUrl = intent.getStringExtra("url")
        when (profileUrl) {
            "default" -> Glide.with(mContext).load(R.drawable.cross).into(like)  // cross değişecek
            else -> Glide.with(mContext).load(profileUrl).into(like)
        }

        Thread {
            try {
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            val mainIntent = Intent(this@LikeActivity, MainActivity::class.java)
            startActivity(mainIntent)
        }.start()

    }

   /* private fun setupToolbar() {
        val tvEx = findViewById(R.id.topNavViewBar) as BottomNavigationViewEx
        TopNavigationViewHelper.setupTopNavigationView(tvEx)
        TopNavigationViewHelper.enableNavigation(mContext, tvEx)
        val menu = tvEx.menu
        val menuItem = menu.getItem(ACTIVITY_NUM)
        menuItem.isChecked = true
    }*/   // burası menu ile ilgili o yüzden yok
}