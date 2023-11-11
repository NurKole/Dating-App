package com.example.loveanddate

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class GalleryActivity : AppCompatActivity() {

    private lateinit var mImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)


        mImage= findViewById<ImageView>(R.id.addImage)

        val mBack = findViewById<ImageButton>(R.id.back)

        mBack.setOnClickListener {

            val i = Intent(this, AccountSettingsActivity::class.java)
            startActivity(i)
        }

        mImage.setOnClickListener { view ->
            if (!checkPermission()) {
                Toast.makeText(this@GalleryActivity, "Please allow access to continue!", Toast.LENGTH_SHORT).show()
                requestPermission()
            } else {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(intent, 1)
            }
        }
        mImage.setImageResource(R.drawable.baseline_add_to_photos_24)

    }


    fun checkPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }

    fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImage = data.data
            mImage.setImageURI(selectedImage)
        }
    }


}