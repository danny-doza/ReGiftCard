package com.csci4480.regiftcard.data.classes

class User {
    //Getters, Setters
    var username: String? = null
    var email_address: String? = null
    var userID: String? = null
    var profile_photo: String? = null

    // Constructors
    constructor() {}

    constructor(username: String?, email_address: String?) {
        this.username = username
        this.email_address = email_address
    }

    constructor(username: String?, userID: String?, email_address: String?, profile_photo: String?) {
        this.username = username
        this.userID = userID
        this.email_address = email_address
        this.profile_photo = profile_photo
    }

    override fun toString(): String {
        return "User {" +
                "user_id ='" + userID + '\'' +
                ", username ='" + username + '\'' +
                ", email_address ='" + email_address +
                '}'
    }
}