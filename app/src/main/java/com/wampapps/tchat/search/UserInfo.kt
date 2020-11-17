package com.wampapps.tchat.search

import java.io.Serializable

class UserInfo: Serializable {

    private var userId: String? = null
    private var status: String? = null
    private var currentChat: String? = null
    private var userRole: String? = null
    private var timeEntered: Long? = null

    constructor()
    constructor(status: String, currentChat: String?){
        this.status = status
        this.currentChat = currentChat
    }

    fun setUserId(userId: String){
        this.userId = userId
    }

    fun getUserId(): String?{
        return this.userId
    }

    fun setUserStatus(status: String){
        this.status = status
    }

    fun getUserStatus(): String?{
        return this.status
    }

    fun setUserCurrentChat(currentChat: String){
        this.currentChat = currentChat
    }

    fun getUserCurrentChat(): String?{
        return this.currentChat
    }

    fun setUserRole(userRole: String){
        this.userRole = userRole
    }

    fun getUserRole(): String?{
        return this.userRole
    }

    fun setUserTimeEntered(timeEntered: Long){
        this.timeEntered = timeEntered
    }

    fun getUserTimeEntered(): Long?{
        return this.timeEntered
    }
}