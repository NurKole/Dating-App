package com.example.loveanddate

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import android.Manifest
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.io.ByteArrayOutputStream
import java.io.IOException

class AccountSettingsActivity : AppCompatActivity() {

    val age_items = arrayOf("18", "19", "20", "21", "22")
    val gender_items = arrayOf("Male", "Female")

    private lateinit var mNameField: EditText
    private lateinit var mAgeField: AutoCompleteTextView
    private lateinit var mGenderField: AutoCompleteTextView
    private lateinit var mDone: Button
    private lateinit var mEdit: Button
    private lateinit var mProfileImage: ImageView
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mUserDatabase: DatabaseReference

    private var name: String? = null
    private var age: String? = null
    private var gender: String? = null
    private var userId: String? = null
    private var profileImageUrl: String? = null
    private var resultUri: Uri? = null
    private var mCustomerDatabase: DatabaseReference? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_settings)

        val userSex = intent.getStringExtra("userSex")

        mNameField=findViewById<EditText>(R.id.Name)
        mAgeField= findViewById<AutoCompleteTextView>(R.id.dropdown_age)
        mGenderField = findViewById<AutoCompleteTextView>(R.id.dropdown_gender)
        mProfileImage= findViewById<ImageView>(R.id.profileImage)
        mDone=findViewById<Button>(R.id.done)
        mAuth= FirebaseAuth.getInstance()
        userId=mAuth.currentUser?.uid

        mCustomerDatabase = FirebaseDatabase.getInstance().reference.child("Users").child(userSex ?: "").child(userId ?: "")


        val adapterAgeItems = ArrayAdapter<String>(this, R.layout.list_age_item, age_items)
        mAgeField.setAdapter(adapterAgeItems)
        mAgeField.setOnItemClickListener { adapterView, view, position, _ ->
            val selected = adapterView.getItemAtPosition(position).toString()

            if (selected <= 18.toString()) {

                val alertDialogBuilder = AlertDialog.Builder(this)
                alertDialogBuilder.setTitle("Warning")
                alertDialogBuilder.setMessage("People who are 18 years old and younger cannot use this application.")
                alertDialogBuilder.setPositiveButton("Okey") { _, _ ->
                    mAgeField.text = null
                }
                val alertDialog = alertDialogBuilder.create()
                alertDialog.show()
            } else {
                Toast.makeText(this, "Selected age: $selected", Toast.LENGTH_SHORT).show()
            }

        }

        val adapterGenderItems = ArrayAdapter<String>(this, R.layout.list_gender_item, gender_items)
        mGenderField.setAdapter(adapterGenderItems)
        mGenderField.setOnItemClickListener { adapterView, view, position, _ ->
            val selected = adapterView.getItemAtPosition(position).toString()
            Toast.makeText(this, "Selected gender: $selected", Toast.LENGTH_SHORT).show()
        }

        getUserInfo()

        val mEdit = findViewById<ImageButton>(R.id.edit_gallery)
        mEdit.setOnClickListener {view ->
            val i = Intent(this, GalleryActivity::class.java)
            startActivity(i)
        }

        mProfileImage.setOnClickListener { view ->
            if (!checkPermission()) {
                Toast.makeText(this@AccountSettingsActivity, "Please allow access to continue!", Toast.LENGTH_SHORT).show()
                requestPermission()
            } else {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(intent, 1)
            }
        }

        mDone.setOnClickListener {
            saveUserInformation()
        }

    }

    fun checkPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }
    fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 100)
    }

    private fun getUserInfo() {
        mCustomerDatabase?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.childrenCount > 0) {
                    val map = dataSnapshot.value as? Map<String, Any>

                    if (map != null) {
                        if (map["name"] != null) {
                            name = map["name"].toString()
                            mNameField.setText(name)
                        }
                        if (map["age"] != null) {
                            age = map["phone"].toString()
                            mAgeField.setText(age)
                        }
                        if (map["gender"] != null) {
                            gender = map["gender"].toString()
                            mGenderField.setText(gender)
                        }
                        if (map["profileImageUrl"] != null) {
                            val profileImageUrl = map["profileImageUrl"].toString()
                            when (profileImageUrl) {
                                "default" -> Glide.with(application).load(R.drawable.profile_foreground).into(mProfileImage)
                                else -> Glide.with(application).load(profileImageUrl).into(mProfileImage)
                            }
                        }

                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }


    private fun saveUserInformation() {
        name = mNameField.text.toString()
        age = mAgeField.text.toString()
        gender = mGenderField.text.toString()

        val userInfo = hashMapOf(
            "name" to name,
            "age" to age,
            "gender" to gender
        )

        mCustomerDatabase?.updateChildren(userInfo as Map<String, Any>)
        if(resultUri != null){
            val filepath: StorageReference = FirebaseStorage.getInstance().reference.child("profileImages").child(userId!!)
            var bitmap: Bitmap? = null

            try {
                bitmap = MediaStore.Images.Media.getBitmap(application.contentResolver, resultUri)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            val baos = ByteArrayOutputStream()
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 20, baos)
            val data: ByteArray = baos.toByteArray()

            val uploadTask: UploadTask = filepath.putBytes(data)
            uploadTask.addOnFailureListener { e ->
                finish()
            }

            uploadTask.addOnSuccessListener { taskSnapshot ->
                val uri = taskSnapshot.storage.downloadUrl
                while (!uri.isComplete);
                val downloadUri = uri.result
                val userInfo = hashMapOf<String, Any>("profileImageUrl" to downloadUri.toString())
                mUserDatabase.updateChildren(userInfo)
                finish()

            }


        }else{
            mCustomerDatabase?.updateChildren(userInfo as Map<String, Any>)
            finish()
         }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = data?.data
            resultUri = imageUri
            mProfileImage.setImageURI(resultUri)
        }
    }

}
