package com.wampapps.tchat.message

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wampapps.tchat.R
import com.github.library.bubbleview.BubbleTextView

class MessageViewHolder: RecyclerView.ViewHolder {

    var messageTime: TextView
    var messageText: BubbleTextView
    var messageImage: ImageView
    var userLeftText: TextView
    var interlocutorTime: TextView
    var interlocutorText: BubbleTextView
    var interlocutorImage: ImageView

    constructor(itemView: View) : super(itemView){
        messageTime = itemView.findViewById(R.id.messageTime)
        messageText = itemView.findViewById(R.id.messageText)
        messageImage = itemView.findViewById(R.id.messageImage)
        userLeftText = itemView.findViewById(R.id.userLeftText)
        interlocutorTime = itemView.findViewById(R.id.messageInterlocutorTime)
        interlocutorText = itemView.findViewById(R.id.messageInterlocutorText)
        interlocutorImage = itemView.findViewById(R.id.messageInterlocutorImage)
    }





}