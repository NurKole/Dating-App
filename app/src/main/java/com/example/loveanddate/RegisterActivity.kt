package com.example.loveanddate

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase


class RegisterActivity : AppCompatActivity() {

    private lateinit var mRegister: Button
    private lateinit var spinner: ProgressBar
    private lateinit var mEmail: EditText
    private lateinit var mPassword: EditText
    private lateinit var mName: EditText
    private lateinit var checkBox: CheckBox
    private lateinit var textView: TextView
    private lateinit var mBudget: EditText
    private lateinit var mRadioGroup: RadioGroup
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firebaseAuthStateListener: FirebaseAuth.AuthStateListener

    private val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
    private val TAG = "RegisterActivity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        spinner = findViewById(R.id.pBar)
        spinner.visibility = View.GONE

        FirebaseApp.initializeApp(this)

        val existing: TextView = findViewById(R.id.existing)
        mAuth = FirebaseAuth.getInstance()
        firebaseAuthStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            spinner.visibility = View.VISIBLE
            val user: FirebaseUser? = FirebaseAuth.getInstance().currentUser
            if (user != null && user.isEmailVerified) {
                val i = Intent(this@RegisterActivity, MainActivity::class.java)
                startActivity(i)
                finish()
                spinner.visibility = View.GONE
                return@AuthStateListener
            }
            spinner.visibility = View.GONE
        }

        existing.setOnClickListener {
            val i = Intent(this@RegisterActivity, MainActivity::class.java)
            startActivity(i)
            finish()
            return@setOnClickListener
        }
        mRegister = findViewById(R.id.register)
        mEmail = findViewById(R.id.email)
        mPassword = findViewById(R.id.password)
        mName = findViewById(R.id.name)
        checkBox = findViewById(R.id.checkbox1)
        textView = findViewById(R.id.TextView2)
        // spinner = findViewById(R.id.pBar) // Replace with your ProgressBar ID

        checkBox.text = ""
        textView.text = Html.fromHtml("I have read and agree to the " +
                "<a href='https://drive.google.com/file/d/1oI3mvZirbjn8K0HhIlH_Z_m3Az-Vlu3a/view?usp=sharing'>Terms & Conditions</a>")
        textView.isClickable = true
        textView.movementMethod = LinkMovementMethod.getInstance()

        mRegister.setOnClickListener {
            spinner.visibility = View.VISIBLE
            val email = mEmail.text.toString()
            val password = mPassword.text.toString()
            val name = mName.text.toString()
            val tnc = checkBox.isChecked

            if (checkInputs(email, name, password, tnc)) {
                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this@RegisterActivity) { task ->
                        if (!task.isSuccessful) {
                            Toast.makeText(this@RegisterActivity, task.exception?.message, Toast.LENGTH_SHORT).show()
                        } else {
                            mAuth.currentUser?.sendEmailVerification()
                                ?.addOnCompleteListener { emailVerificationTask ->
                                    if (emailVerificationTask.isSuccessful) {
                                        Toast.makeText(
                                            this@RegisterActivity,
                                            "Registration successfully. Please check your email for verification.",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        val userId = mAuth.currentUser?.uid
                                        val currentUserDb =
                                            FirebaseDatabase.getInstance().reference.child("Users").child(userId!!)
                                        val userInfo = HashMap<String, Any>()
                                        userInfo["name"] = name
                                        userInfo["profileImageUrl"] = "default"
                                        currentUserDb.updateChildren(userInfo)

                                        mEmail.setText("")
                                        mName.setText("")
                                        mPassword.setText("")

                                        val intent = Intent(this@RegisterActivity, LoginOrReg::class.java)
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        Toast.makeText(
                                            this@RegisterActivity,
                                            emailVerificationTask.exception?.message,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        }
                    }
            }

            spinner.visibility= View.GONE

        }

    }

    private fun checkInputs(email: String, username: String, password: String, tnc: Boolean): Boolean {
        if (email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "All fields must be filled out", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!email.matches(emailPattern.toRegex())) {
            Toast.makeText(this, "Invalid email address, enter a valid email ID and click on confirm", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!tnc) {
            Toast.makeText(this, "Please accept Terms and Conditions", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
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
        super.onBackPressed()
        val i = Intent(this@RegisterActivity, LoginOrReg::class.java)
        startActivity(i)
        finish()
    }

}