package com.wampapps.tchat.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wampapps.tchat.R
import com.wampapps.tchat.message.Message
import com.wampapps.tchat.message.MessageDataAdapter
import com.wampapps.tchat.search.UserInfo
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import com.google.firebase.firestore.DocumentChange.Type.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.ArrayList

class ChatActivity : AppCompatActivity() {
    private companion object{
        const val CAMERA_REQUEST_CODE: Int=100
        const val STORAGE_REQUEST_CODE: Int= 200

        const val IMAGE_PICK_CAMERA_CODE: Int=300
        const val IMAGE_PICK_STORAGE_CODE: Int=400
    }

    private var cameraPermissions:Array<String> = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private var storagePermissions:Array<String> = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    private var messages:ArrayList<Message> = ArrayList()

    private lateinit var arguments: Bundle
    private lateinit var user: UserInfo
    private lateinit var userPushId: String

    private lateinit var chatId: String
    private lateinit var userRole: String

    private var db:FirebaseFirestore = FirebaseFirestore.getInstance()
    private var dbRefChats: CollectionReference = db.collection("Chats")
    private var dbRefUsers: CollectionReference = db.collection("Users")

    private lateinit var activity_main: RelativeLayout
    private lateinit var emojiconEditText: EmojiconEditText
    private lateinit var emojIconActions: EmojIconActions
    private lateinit var emojiButton: ImageView
    private lateinit var submitButton: ImageView
    private lateinit var addMediaButton: ImageView
    private lateinit var recyclerMessages: RecyclerView

    private var image_rui: Uri? = null

    private lateinit var adView: AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments = intent.extras!!
        user = arguments.getSerializable(UserInfo::class.java.simpleName) as UserInfo
        userPushId = arguments.getSerializable("userPushId").toString()
        chatId = arguments.getSerializable("chatId").toString()
        userRole = arguments.getSerializable("userRole").toString()

        setContentView(R.layout.activity_chat)

        adView = findViewById(R.id.chatAdView)
        val adRequest: AdRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        activity_main=findViewById(R.id.activity_main)
        recyclerMessages = findViewById(R.id.recyclerOfMessages)
        submitButton=findViewById(R.id.submitButton)
        emojiButton=findViewById(R.id.emojiButton)
        emojiconEditText=findViewById(R.id.textField)
        addMediaButton=findViewById(R.id.addMediaButton)
        emojIconActions=EmojIconActions(application, activity_main, emojiconEditText, emojiButton)
        emojIconActions.ShowEmojIcon()

        val manager = LinearLayoutManager(this)
        manager.stackFromEnd = true
        recyclerMessages.layoutManager = manager

        dbRefChats
            .document(chatId)
            .collection("messages")
            .addSnapshotListener{value: QuerySnapshot?, error: FirebaseFirestoreException? ->
                if (error != null) {
                    Toast.makeText(this, "ERROR: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                for(dc in value!!.documentChanges){
                    when(dc.type){
                        ADDED -> {
                            val message: Message = hashToMessage(dc.document.data)
                            messages.add(message)

                            if(message.getTypeMessage()=="disconnect"){
                                emojiButton.visibility = View.GONE
                                emojiconEditText.visibility = View.GONE
                                addMediaButton.visibility = View.GONE
                                submitButton.visibility = View.GONE
                            }
                        }
                        MODIFIED -> {}
                        REMOVED -> {}
                    }
                }
                val adapter = MessageDataAdapter(
                    this@ChatActivity,
                    messages,
                    user.getUserId().toString()
                )
                adapter.notifyDataSetChanged()

                recyclerMessages.adapter=adapter
        }

        submitButton.setOnClickListener {
            if(image_rui==null) {
                if(emojiconEditText.text.toString() == ""){
                    Toast.makeText(this, "Введите сообщение", Toast.LENGTH_LONG).show()
                }else {
                    db.collection("Chats").document(chatId).collection("messages")
                        .add(
                            Message(
                                user.getUserId().toString(),
                                emojiconEditText.text.toString(),
                                "text"
                            )
                        )
                        .addOnSuccessListener {
                            //Toast.makeText(this, "message sent", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "ERROR:" + it.message, Toast.LENGTH_SHORT).show()
                        }

                    emojiconEditText.setText("")
                }
            }else{
                uploadImage(image_rui)

                addMediaButton.setImageResource(R.drawable.ic_add_media_button)

                emojiconEditText.setText("")

                image_rui=null
            }
        }

        addMediaButton.setOnClickListener {
            showMediaPickDialog()
        }
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
        dbRefUsers.document(userPushId).update("userCurrentChat", "")
        dbRefUsers.document(userPushId).update("userStatus", "free")

        if(userRole == "user"){
            dbRefChats.document(chatId).update("userId", "")
        }else{
            dbRefChats.document(chatId).update("interlocutorId", "")
        }

        dbRefChats.document(chatId).collection("messages")
            .add(Message(
                "",
                "",
                "disconnect"
            ))

        chatId = ""
    }

    private fun uploadImage(uri: Uri?) {
        val progressDialog=ProgressDialog(this)
        progressDialog.setTitle("Загрузка")
        progressDialog.show()

        val path:String = Date().time.toString()
        val ref:StorageReference = FirebaseStorage.getInstance().reference.child("images/$path")

        val bmp: Bitmap = MediaStore.Images.Media.getBitmap(contentResolver, image_rui)
        val baos = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 20, baos)
        val data: ByteArray = baos.toByteArray()

        ref.putBytes(data)
            .addOnSuccessListener {
                progressDialog.dismiss()

                val uriTask: Task<Uri> = it.storage.downloadUrl
                while(!uriTask.isSuccessful);
                val downloadUri:String =uriTask.result.toString()

                if(uriTask.isSuccessful){
                    db.collection("Chats").document(chatId).collection("messages")
                        .add(Message(
                            user.getUserId().toString(),
                            downloadUri,
                            "image"
                        ))
                        .addOnSuccessListener {
                            Toast.makeText(this, "Успешно загружено", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "ERROR: "+it.message, Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, ""+it.message, Toast.LENGTH_SHORT).show()
            }
            .addOnProgressListener {
                val progress:Double=(100.0*it.bytesTransferred)/it.totalByteCount
                progressDialog.setMessage("Загружено "+progress.toInt()+"%...")
            }

        addMediaButton.setImageResource(R.drawable.ic_add_media_button)
    }

    private fun hashToMessage(map: MutableMap<String, Any>): Message{
        return Message(
            map.getValue("messageUserId") as String,
            map.getValue("textMessage") as String,
            map.getValue("typeMessage") as String
        )
    }

    private fun showMediaPickDialog() {
        val options: Array<String> = arrayOf("Камеры", "Галереи")

        val builder: AlertDialog.Builder=AlertDialog.Builder(this)
        builder.setTitle("Изображение из:")
        builder.setItems(options) { _, i ->
            if(i==0){
                //camera
                if(!checkCameraPermission()) requestCameraPermission()
                else pickFromCamera()
            }
            if(i==1){
                //gallery
                if(!checkStoragePermission()) requestStoragePermission()
                else pickFromGallery()
            }
        }
        builder.create().show()
    }

    private fun pickFromGallery() {
        val intent =Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent,
            IMAGE_PICK_STORAGE_CODE
        )
    }

    private fun pickFromCamera() {
        val cv = ContentValues()

        image_rui = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv)!!

        val intent =Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_rui)
        startActivityForResult(intent,
            IMAGE_PICK_CAMERA_CODE
        )
    }

    private fun checkStoragePermission():Boolean{
        return ContextCompat.checkSelfPermission(this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED)
    }

    private fun requestStoragePermission(){
        ActivityCompat.requestPermissions(this, storagePermissions,
            STORAGE_REQUEST_CODE
        )
    }

    private fun checkCameraPermission():Boolean{
        val result:Boolean=ContextCompat.checkSelfPermission(this,
            Manifest.permission.CAMERA)==(PackageManager.PERMISSION_GRANTED)
        val result1:Boolean=ContextCompat.checkSelfPermission(this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED)

        return result&&result1
    }

    private fun requestCameraPermission(){
        ActivityCompat.requestPermissions(this, cameraPermissions,
            CAMERA_REQUEST_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK){
            if(requestCode == IMAGE_PICK_STORAGE_CODE){
                image_rui = data?.data!!

                addMediaButton.setImageResource(R.drawable.ic_added_media_button)
            }else if(requestCode == IMAGE_PICK_CAMERA_CODE){
                addMediaButton.setImageResource(R.drawable.ic_added_media_button)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode){
            CAMERA_REQUEST_CODE -> {
                if(grantResults.isNotEmpty()){
                    val cameraAccepted:Boolean=grantResults[0]==PackageManager.PERMISSION_GRANTED
                    val storageAccepted:Boolean=grantResults[1]==PackageManager.PERMISSION_GRANTED

                    if(cameraAccepted&&storageAccepted) pickFromCamera()
                    else Toast.makeText(this, "Обязательно нужны разрешения на использование камеры и хранилища", Toast.LENGTH_SHORT).show()
                }
            }
            STORAGE_REQUEST_CODE ->{
                if(grantResults.isNotEmpty()){
                    val writeStorageAccepted:Boolean=grantResults[0]==PackageManager.PERMISSION_GRANTED

                    if(writeStorageAccepted) pickFromGallery()
                    else Toast.makeText(this, "Обязательно нужно разрешение на использование хранилища", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}