package com.wampapps.tchat.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.wampapps.tchat.R
import com.wampapps.tchat.search.UserInfo
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*


class StartActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth

    private lateinit var searchButton: Button

    var user = UserInfo()
    private var userPushId: String = UUID.randomUUID().toString()

    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private lateinit var adView: AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //initializing ads here
        MobileAds.initialize(this){}

        setContentView(R.layout.activity_start)

        //getting current user
        mAuth = FirebaseAuth.getInstance()

        //assigning views with variables
        searchButton = findViewById(R.id.searchButton)

        adView = findViewById(R.id.startAdView)

        //getting an ad and loading it
        val adRequest: AdRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        //setting up a user
        user.setUserId(userPushId)
        user.setUserStatus("free")

        searchButton.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            val extras = Bundle()
            extras.putSerializable(UserInfo::class.java.simpleName, user)

            //setting up a user
            user.setUserCurrentChat("")
            user.setUserRole("")
            user.setUserTimeEntered(Date().time)

            //uploading a user's node to the database
            db.collection("Users").document(userPushId).set(user)

            //keeping a user's id(in this case pushId==id)
            extras.putString("userPushId", userPushId)
            intent.putExtras(extras)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()

        val currentUser = mAuth.currentUser
        updateUI(currentUser)
    }

    override fun onResume() {
        super.onResume()

        adView.resume()
    }

    override fun onPause() {
        adView.pause()

        super.onPause()
    }

    override fun onDestroy() {
        adView.destroy()

        super.onDestroy()
    }

    //found in web, used for logging in
    private fun updateUI(currentUser: FirebaseUser?) {
        if(currentUser == null){
            mAuth.signInAnonymously()
                .addOnCompleteListener {
                    if(it.isSuccessful){
                        val user = mAuth.currentUser
                        updateUI(user)
                    }else{
                        updateUI(null)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                }
        }
    }
}