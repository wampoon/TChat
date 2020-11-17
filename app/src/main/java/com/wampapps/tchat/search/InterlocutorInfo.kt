package com.wampapps.tchat.search

class InterlocutorInfo {

    private var interlocutorEmail: String? = null
    private var interlocutorGender: String? = null
    private var interlocutorAge: String? = null

    constructor()
    constructor(interlocutorGender: String, interlocutorAge: String){
        this.interlocutorGender = interlocutorGender
        this.interlocutorAge = interlocutorAge
    }

    fun setInterlocutorEmail(interlocutorEmail: String){
        this.interlocutorEmail = interlocutorEmail
    }

    fun getInterlocutorEmail(): String?{
        return this.interlocutorEmail
    }

    fun setInterlocutorGender(interlocutorGender: String){
        this.interlocutorGender = interlocutorGender
    }

    fun getInterlocutorGender(): String?{
        return this.interlocutorGender
    }

    fun setInterlocutorAge(interlocutorAge: String){
        this.interlocutorAge = interlocutorAge
    }

    fun getInterlocutorAge(): String?{
        return this.interlocutorAge
    }
}