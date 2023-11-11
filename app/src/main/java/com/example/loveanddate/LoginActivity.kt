package com.example.loveanddate

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private val TAG = "LoginActivity"
    private lateinit var spinner: ProgressBar
    private lateinit var mLogin: Button
    private lateinit var mEmail: EditText
    private lateinit var mPassword: EditText
    private lateinit var mForgetPassword: TextView
    private var loginBtnClicked: Boolean = false
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firebaseAuthStateListener: FirebaseAuth.AuthStateListener



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginBtnClicked = false
        spinner = findViewById(R.id.pBar) as ProgressBar
        spinner.visibility = View.GONE
        mAuth = FirebaseAuth.getInstance()
        mLogin = findViewById(R.id.login) as Button
        mEmail = findViewById(R.id.email) as EditText
        mPassword = findViewById(R.id.password) as EditText
        mForgetPassword = findViewById(R.id.forgetPasswordButton) as TextView

        mLogin.setOnClickListener {
            loginBtnClicked = true
            spinner.visibility = View.VISIBLE
            val email = mEmail.text.toString()
            val password = mPassword.text.toString()

            if (isStringNull(email)|| isStringNull(password) ) {

                Toast.makeText(this@LoginActivity, "You must fill out all the fields", Toast.LENGTH_SHORT).show()
            } else{
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this@LoginActivity) { task ->
                    if (!task.isSuccessful) {
                        Toast.makeText(this@LoginActivity, task.exception?.message, Toast.LENGTH_SHORT).show()
                    } else {
                        if (mAuth.currentUser?.isEmailVerified == true) {
                            val i = Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(i)
                            finish()
                        } else {
                            Toast.makeText(this@LoginActivity, "Please verify your email", Toast.LENGTH_SHORT).show()
                        }
                    }
                    spinner.visibility =View.GONE
                }

            }


        }

        mForgetPassword.setOnClickListener {
            spinner.visibility = View.VISIBLE
            val intent = Intent(this@LoginActivity, ForgetPasswordActivity::class.java)
            startActivity(intent)
            finish()
        }

        firebaseAuthStateListener = FirebaseAuth.AuthStateListener {
                firebaseAuth -> val user = FirebaseAuth.getInstance().currentUser
            if (user != null && user.isEmailVerified && !loginBtnClicked) {
                spinner.visibility = View.VISIBLE
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
                spinner.visibility = View.GONE
                return@AuthStateListener
            }
        }

    }

    private fun isStringNull(email: String): Boolean {
        return email.equals("")
    }

    override fun onStart() {
        super.onStart()
        mAuth.addAuthStateListener(firebaseAuthStateListener)
    }

    override fun onStop() {
        super.onStop()
        mAuth.removeAuthStateListener(firebaseAuthStateListener)
    }

    override fun onBackPressed() {
        val intent = Intent(this@LoginActivity, LoginOrReg::class.java)
        startActivity(intent)
        finish()
        return
    }

}