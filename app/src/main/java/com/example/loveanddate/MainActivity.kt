package com.example.loveanddate

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import com.example.loveanddate.Cards.Cards
import com.example.loveanddate.Cards.arrayAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.lorentzos.flingswipe.SwipeFlingAdapterView
import com.onesignal.OneSignal

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    private lateinit var cards_data: Array<Cards>
    private lateinit var arrayAdapter: arrayAdapter
    private var i: Int = 0
    private var tag: String? = null
    private lateinit var mAuth: FirebaseAuth
    private var firstStart: Boolean = false
    private lateinit var currentUId: String
    private lateinit var notification: String
    private lateinit var sendMessageText: String
    private var activityStarted: Boolean = false
    private lateinit var usersDb: DatabaseReference
    private lateinit var listView: ListView
    private var rowItems: MutableList<Cards> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val accountButton = findViewById<ImageButton>(R.id.account)

        accountButton.setOnClickListener {

            val i = Intent(this, AccountSettingsActivity::class.java)
            startActivity(i)
        }

        val prefs: SharedPreferences = getSharedPreferences("prefs", MODE_PRIVATE)
        firstStart = prefs.getBoolean("firstStart", true)
        //setupTopNavigationView()
        if (intent.extras != null) {
            for (key: String in intent.extras!!.keySet()) {
                val value: Any? = intent.extras!!.get(key)
                Log.d(TAG, "Key: $key Value: $value")
            }

       /* fun goToSettings(view: View) {
            val intent = Intent(this, AccountSettingsActivity::class.java)
            intent.putExtra("userSex", userSex)
            startActivity(intent)
        }*/


        }

        tag = "MainActivity"
        usersDb = FirebaseDatabase.getInstance().getReference().child("Users")
        mAuth = FirebaseAuth.getInstance()
        if (mAuth != null && mAuth.currentUser != null) {
            currentUId = mAuth.currentUser!!.uid
        } else {
            Log.d(tag, "Authorization failed")
            Toast.makeText(applicationContext, "Auth failed", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        arrayAdapter = arrayAdapter(this, R.layout.item, rowItems)
        val flingContainer: SwipeFlingAdapterView = findViewById(R.id.frame)
        flingContainer.adapter = arrayAdapter
        flingContainer.setFlingListener(object : SwipeFlingAdapterView.onFlingListener {
            override fun removeFirstObjectInAdapter() {
                Log.d(tag, "removed object!")
                rowItems.removeAt(0)
                arrayAdapter.notifyDataSetChanged()
            }

            override fun onLeftCardExit(dataObject: Any) {
                val obj: Cards = dataObject as Cards
                val userId: String = obj.userId
                usersDb.child(userId).child("connections").child("nope").child(currentUId).setValue(true)
                Toast.makeText(this@MainActivity, "Left", Toast.LENGTH_SHORT).show()
                val tv: TextView = findViewById(R.id.noCardsBanner)
                if (rowItems.size == 0) {
                    tv.visibility = View.VISIBLE
                } else {
                    tv.visibility = View.INVISIBLE
                }
            }

            override fun onRightCardExit(dataObject: Any) {
                val obj: Cards = dataObject as Cards
                val userId: String = obj.userId
                usersDb.child(userId).child("connections").child("yeps").child(currentUId).setValue(true)
                isConnectionMatch(userId)
                Toast.makeText(this@MainActivity, "Right", Toast.LENGTH_SHORT).show()
                val tv: TextView = findViewById(R.id.noCardsBanner)
                if (rowItems.size == 0) {
                    tv.visibility = View.VISIBLE
                } else {
                    tv.visibility = View.INVISIBLE
                }
            }

            override fun onAdapterAboutToEmpty(itemsInAdapter: Int) {
            }

            override fun onScroll(scrollProgressPercent: Float) {
                val view: View = flingContainer.getSelectedView()
                view.findViewById<View>(R.id.item_swipe_right_indicator).alpha =
                    if (scrollProgressPercent < 0) -scrollProgressPercent else 0.toFloat()
                view.findViewById<View>(R.id.item_swipe_left_indicator).alpha =
                    if (scrollProgressPercent > 0) scrollProgressPercent else 0.toFloat()
            }
        })

        flingContainer.setOnItemClickListener(object : SwipeFlingAdapterView.OnItemClickListener {
            override fun onItemClicked(itemPosition: Int, dataObject: Any) {
                Toast.makeText(this@MainActivity, "Item Clicked", Toast.LENGTH_SHORT).show()
            }
        })

    }
    fun dislikeBtn(v: View) {
        if (rowItems.isNotEmpty()) {
            val cardItem: Cards = rowItems[0]
            val userId: String = cardItem.userId
            usersDb.child(userId).child("connections").child("nope").child(currentUId).setValue(true)
            Toast.makeText(this@MainActivity, "Left", Toast.LENGTH_SHORT).show()
            rowItems.removeAt(0)
            arrayAdapter.notifyDataSetChanged()
            val tv: TextView = findViewById(R.id.noCardsBanner)
            if (rowItems.isEmpty()) {
                tv.visibility = View.VISIBLE
            } else {
                tv.visibility = View.INVISIBLE
            }
            val btnClick = Intent(this@MainActivity, DislikeActivity::class.java)
            btnClick.putExtra("url", cardItem.profileImageUrl)
            startActivity(btnClick)
        }
    }

    fun likeBtn(v: View) {
        if (rowItems.isNotEmpty()) {
            val cardItem: Cards = rowItems[0]
            val userId: String = cardItem.userId
            usersDb.child(userId).child("connections").child("yeps").child(currentUId).setValue(true)
            isConnectionMatch(userId)
            Toast.makeText(this@MainActivity, "Right", Toast.LENGTH_SHORT).show()
            rowItems.removeAt(0)
            arrayAdapter.notifyDataSetChanged()
            val tv: TextView = findViewById(R.id.noCardsBanner)
            if (rowItems.isEmpty()) {
                tv.visibility = View.VISIBLE
            } else {
                tv.visibility = View.INVISIBLE
            }
            val btnClick = Intent(this@MainActivity, LikeActivity::class.java)
            btnClick.putExtra("url", cardItem.profileImageUrl)
            startActivity(btnClick)
        }
    }
    private fun isConnectionMatch(userId: String) {
        val currentUserConnectionsDb: DatabaseReference = usersDb.child(currentUId).child("connections").child("yeps").child(userId)
        usersDb.child(currentUId).child("name").addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    sendMessageText = dataSnapshot.value.toString()
                } else {
                    sendMessageText = ""
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}

        })
        if (currentUId != userId) {
            currentUserConnectionsDb.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Toast.makeText(this@MainActivity, "New Connection", Toast.LENGTH_LONG).show()
                        val key: String? = FirebaseDatabase.getInstance().reference.child("Chat").push().key
                        val mapLastTimeStamp: MutableMap<String, Any> = HashMap()
                        val now: Long = System.currentTimeMillis()
                        val timeStamp: String = now.toString()
                        mapLastTimeStamp["lastTimeStamp"] = timeStamp
                        usersDb.child(dataSnapshot.key!!).child("connections").child("matches")
                            .child(currentUId).child("ChatId").setValue(key)
                        usersDb.child(dataSnapshot.key!!).child("connections").child("matches")
                            .child(currentUId).updateChildren(mapLastTimeStamp)
                        usersDb.child(currentUId).child("connections").child("matches")
                            .child(dataSnapshot.key!!).child("ChatId").setValue(key)
                        usersDb.child(currentUId).child("connections").child("matches")
                            .child(dataSnapshot.key!!).updateChildren(mapLastTimeStamp)
                        notification = " "
                        val notificationID: DatabaseReference = FirebaseDatabase.getInstance().getReference()
                            .child("Users").child(userId).child("notificationKey")
                        notificationID.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    notification = snapshot.value.toString()
                                    Log.d("sendChat", notification)
                                    //SendNotification("You have a new match!", "", notification, null, null)
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {}
                        })
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }
    }

    private lateinit var userNeed: String
    private lateinit var userGive: String
    private lateinit var oppositeUserNeed: String
    private lateinit var oppositeUserGive: String
    private fun checkUserSex() {
        val user: FirebaseUser = FirebaseAuth.getInstance().currentUser!!
        val userDb: DatabaseReference = usersDb.child(user.uid)
        userDb.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.child("need").value != null) {
                        userNeed = dataSnapshot.child("need").value.toString()
                        userGive = dataSnapshot.child("give").value.toString()
                        oppositeUserGive = userNeed
                        oppositeUserNeed = userGive
                        getOppositeSexUsers(oppositeUserGive, oppositeUserNeed)
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun getOppositeSexUsers(oppositeUserGive: String, oppositeUserNeed: String) {
        usersDb.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                if (dataSnapshot.exists() && dataSnapshot.key != currentUId) {
                    if (dataSnapshot.child("give").exists() && dataSnapshot.child("need").exists() && !dataSnapshot.child("connections")
                            .child("nope").hasChild(currentUId) && !dataSnapshot.child("connections")
                            .child("yeps").hasChild(currentUId) && dataSnapshot.child("give").value.toString() == oppositeUserGive && dataSnapshot.child("need").value.toString() == oppositeUserNeed
                    ) {
                        var profileImageUrl = "default"
                        if (dataSnapshot.child("profileImageUrl").value != "default") {
                            profileImageUrl = dataSnapshot.child("profileImageUrl").value.toString()
                        }
                        val item = Cards(
                            dataSnapshot.key!!,
                            dataSnapshot.child("name").value.toString(),
                            profileImageUrl,
                            dataSnapshot.child("age").value.toString()

                        )
                        rowItems.add(item)
                        arrayAdapter.notifyDataSetChanged()
                    } else if (dataSnapshot.child("give").exists() && !dataSnapshot.child("connections")
                            .child("nope").hasChild(currentUId) && !dataSnapshot.child("connections")
                            .child("yeps").hasChild(currentUId) && dataSnapshot.child("give").value.toString() == oppositeUserGive
                    ) {
                        var profileImageUrl = "default"
                        if (dataSnapshot.child("profileImageUrl").value != "default") {
                            profileImageUrl = dataSnapshot.child("profileImageUrl").value.toString()
                        }
                        val item = Cards(
                            dataSnapshot.key!!,
                            dataSnapshot.child("name").value.toString(),
                            profileImageUrl,
                            dataSnapshot.child("age").value.toString()
                        )
                        rowItems.add(item)
                        arrayAdapter.notifyDataSetChanged()
                    } else if (dataSnapshot.child("need").exists() && !dataSnapshot.child("connections")
                            .child("nope").hasChild(currentUId) && !dataSnapshot.child("connections")
                            .child("yeps").hasChild(currentUId) && dataSnapshot.child("need").value.toString() == oppositeUserNeed
                    ) {
                        var profileImageUrl = "default"
                        if (dataSnapshot.child("profileImageUrl").value != "default") {
                            profileImageUrl = dataSnapshot.child("profileImageUrl").value.toString()
                        }
                        val item = Cards(
                            dataSnapshot.key!!,
                            dataSnapshot.child("name").value.toString(),
                            profileImageUrl,
                            dataSnapshot.child("age").value.toString()
                        )
                        rowItems.add(item)
                        arrayAdapter.notifyDataSetChanged()
                    }
                }
                val tv: TextView = findViewById(R.id.noCardsBanner)
                if (rowItems.size == 0) {
                    tv.visibility = View.VISIBLE
                } else {
                    tv.visibility = View.INVISIBLE
                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {}

            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }



}