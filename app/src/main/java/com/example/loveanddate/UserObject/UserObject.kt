package com.example.loveanddate.UserObject

import java.io.Serializable

data class UserObject(
    var name: String? = null,
    var age: String? = null,
    var notificationKey: String? = null,
    var selected: Boolean = false
) : Serializable