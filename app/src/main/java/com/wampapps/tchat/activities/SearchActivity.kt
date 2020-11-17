package com.wampapps.tchat.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.wampapps.tchat.R
import com.wampapps.tchat.search.UserInfo
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class SearchActivity : AppCompatActivity() {

    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var dbRefChats: CollectionReference = db.collection("Chats")
    private var dbRefUsers: CollectionReference = db.collection("Users")

    private lateinit var arguments: Bundle
    private lateinit var user: UserInfo
    private lateinit var userPushId: String

    private lateinit var chatId: String
    private var userRole: String = ""
    private var chatCounter = 0

    private lateinit var loadingText: TextView
    private lateinit var loadingPicture: ImageView
    private lateinit var toChatButton: Button

    private lateinit var adView: AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_search)

        arguments = intent.extras!!
        user = arguments.getSerializable(UserInfo::class.java.simpleName) as UserInfo
        userPushId = arguments.getSerializable("userPushId").toString()

        adView = findViewById(R.id.searchAdView)
        val adRequest: AdRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        loadingPicture = findViewById(R.id.loadingPicture)
        loadingText = findViewById(R.id.loadingText)
        toChatButton = findViewById(R.id.toChatButton)

        toChatButton.visibility = View.GONE

        db.collection("Users")
            .document(userPushId)
            .addSnapshotListener { value, _ ->
                if(value!= null && value.exists()){
                    chatId = value.getString("userCurrentChat")!!
                    userRole = value.getString("userRole")!!

                    if(chatId!=""){
                        Toast.makeText(this, "Собеседник найден", Toast.LENGTH_SHORT).show()
                        toChatButton.visibility = View.VISIBLE
                    }
                }

                chatCounter++
            }

        toChatButton.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            val extras = Bundle()
            extras.putSerializable(UserInfo::class.java.simpleName, user)
            extras.putString("userPushId", userPushId)
            extras.putString("chatId", chatId)
            extras.putString("userRole", userRole)
            intent.putExtras(extras)
            startActivity(intent)

            chatId = ""
        }
    }

    override fun onRestart() {
        super.onRestart()

        finish()
    }

    override fun onResume() {
        super.onResume()

        adView.resume()
    }

    override fun onPause() {
        adView.pause()

        super.onPause()

        dbRefUsers.document(userPushId).delete()
    }

    override fun onDestroy() {
        adView.destroy()

        super.onDestroy()

        dbRefUsers.document(userPushId).delete()
    }
}