package com.wampapps.tchat.message

import java.util.Date

class Message {

    private var userId: String? = null
    private var textMessage:String? = null
    private var messageTime: Long? = null
    private var type:String? = null

    constructor(){}
    constructor(_userId:String, _textMessage:String, _type:String){
        userId = _userId
        textMessage=_textMessage
        messageTime=Date().time
        type=_type
    }

    fun getMessageUserId():String?{
        return userId
    }

    fun setMessageUserId(userId:String){
        this.userId = userId
    }

    fun getMessageTime():Long?{
        return messageTime;
    }

    fun setMessageTime(messageTime:Long){
        this.messageTime = messageTime
    }

    fun getTextMessage():String?{
        return textMessage;
    }
    fun setTextMessage(messageText:String){
        this.textMessage = messageText
    }

    fun getTypeMessage():String?{
        return type;
    }
    fun setTypeMessage(messageType:String){
        this.type = messageType
    }
}