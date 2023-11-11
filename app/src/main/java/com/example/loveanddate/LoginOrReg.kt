package com.example.loveanddate

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class LoginOrReg : AppCompatActivity() {

    private lateinit var mLogin: Button
    private lateinit var mRegister: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_or_reg)

        val loginButton = findViewById<Button>(R.id.Login)
        val registerButton = findViewById<Button>(R.id.Register)

        loginButton.setOnClickListener {
            val i = Intent(this@LoginOrReg, LoginActivity::class.java)
            startActivity(i)
        }

        registerButton.setOnClickListener {
            val i = Intent(this@LoginOrReg, RegisterActivity::class.java)
            startActivity(i)
        }
    }
}