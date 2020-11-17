package com.wampapps.tchat.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.wampapps.tchat.R
import com.wampapps.tchat.search.InterlocutorInfo
import com.wampapps.tchat.search.UserInfo
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*


class StartActivity : AppCompatActivity() {

    private companion object{
        const val SIGN_IN_CODE: Int = 1
    }

    private lateinit var mAuth: FirebaseAuth

    private lateinit var searchButton: Button;
    private lateinit var userGenderRadio: RadioGroup
    private lateinit var userAgeRadio: RadioGroup
    private lateinit var interlocutorGenderRadio: RadioGroup
    private lateinit var interlocutorAgeRadio: RadioGroup

    var user = UserInfo()
    var interlocutor = UserInfo()
    private var userPushId: String = UUID.randomUUID().toString()

    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private lateinit var adView: AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MobileAds.initialize(this){}

        setContentView(R.layout.activity_start)

        mAuth = FirebaseAuth.getInstance()

        searchButton = findViewById(R.id.searchButton)

        adView = findViewById(R.id.startAdView)
        val adRequest: AdRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        user.setUserId(userPushId)
        user.setUserStatus("free")

        searchButton.setOnClickListener {
            val intent: Intent = Intent(this, SearchActivity::class.java)
            val extras = Bundle()
            extras.putSerializable(UserInfo::class.java.simpleName, user)

            user.setUserCurrentChat("")
            user.setUserRole("")
            user.setUserTimeEntered(Date().time)

            db.collection("Users").document(userPushId).set(user)

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == SIGN_IN_CODE){
            if(resultCode == Activity.RESULT_OK){
                Toast.makeText(this@StartActivity, "Вы вошли", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this@StartActivity, "Вы не вошли", Toast.LENGTH_SHORT).show()

                searchButton.visibility = View.GONE
                userGenderRadio.visibility = View.GONE
                userAgeRadio.visibility = View.GONE
                interlocutorGenderRadio.visibility = View.GONE
                interlocutorAgeRadio.visibility = View.GONE

                finish()
            }
        }
    }
}