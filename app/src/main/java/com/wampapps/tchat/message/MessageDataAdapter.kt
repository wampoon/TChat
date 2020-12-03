package com.wampapps.tchat.message

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.wampapps.tchat.R
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MessageDataAdapter: RecyclerView.Adapter<MessageViewHolder> {

    private var messagesList: ArrayList<Message>

    private var userId: String

    private var inflater:LayoutInflater

    constructor(context: Context, messagesList: ArrayList<Message>, userId: String) : super() {
        this.messagesList = messagesList
        this.inflater = LayoutInflater.from(context)
        this.userId = userId
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view: View = inflater.inflate(R.layout.list_image_text_item, parent, false)

        return MessageViewHolder(view)
    }

    override fun getItemCount(): Int {
        return messagesList.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    //mainly gets the message type and changes the views to get a correct display
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message: Message = messagesList[position]

        when {
            message.getTypeMessage() == "image" -> {
                if(message.getMessageUserId() == userId){
                    holder.interlocutorTime.visibility = View.GONE
                    holder.interlocutorText.visibility = View.GONE
                    holder.interlocutorImage.visibility = View.GONE

                    holder.messageImage.visibility = View.VISIBLE
                    holder.messageText.visibility = View.GONE
                    holder.messageTime.visibility = View.VISIBLE

                    Picasso.get().load(message.getTextMessage()).placeholder(R.drawable.ic_image_black).into(holder.messageImage)
                }else{
                    holder.interlocutorTime.visibility = View.VISIBLE
                    holder.interlocutorText.visibility = View.GONE
                    holder.interlocutorImage.visibility = View.VISIBLE

                    holder.messageImage.visibility = View.GONE
                    holder.messageText.visibility = View.GONE
                    holder.messageTime.visibility = View.GONE

                    Picasso.get().load(message.getTextMessage()).placeholder(R.drawable.ic_image_black).into(holder.interlocutorImage)
                }
            }
            message.getTypeMessage() == "text" -> {
                if(message.getMessageUserId() == userId){
                    //showing user's message
                    holder.interlocutorTime.visibility = View.GONE
                    holder.interlocutorText.visibility = View.GONE
                    holder.interlocutorImage.visibility = View.GONE

                    holder.messageImage.visibility = View.GONE
                    holder.messageText.visibility = View.VISIBLE
                    holder.messageTime.visibility = View.VISIBLE

                    holder.messageText.text = (message.getTextMessage())
                }else{
                    //showing interlocutor`s message
                    holder.interlocutorTime.visibility = View.VISIBLE
                    holder.interlocutorText.visibility = View.VISIBLE
                    holder.interlocutorImage.visibility = View.GONE

                    holder.messageImage.visibility = View.GONE
                    holder.messageText.visibility = View.GONE
                    holder.messageTime.visibility = View.GONE

                    holder.interlocutorText.text = (message.getTextMessage())
                }
            }
            else -> {
                holder.messageImage.visibility = View.GONE
                holder.messageText.visibility = View.GONE
                holder.messageTime.visibility = View.GONE
                holder.interlocutorImage.visibility = View.GONE
                holder.interlocutorText.visibility = View.GONE
                holder.interlocutorTime.visibility = View.GONE
                holder.userLeftText.visibility = View.VISIBLE
            }
        }

        holder.messageTime.text = convertLongToTime(message.getMessageTime()!!)
        holder.interlocutorTime.text = convertLongToTime(message.getMessageTime()!!)
    }

    @SuppressLint("SimpleDateFormat")
    private fun convertLongToTime(time: Long): String{
        val date = Date(time)
        val format = SimpleDateFormat("dd/M/yyyy hh:mm:ss")

        return format.format(date)
    }
}