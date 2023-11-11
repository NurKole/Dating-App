package com.example.loveanddate.Cards

class Cards {
    var userId: String = ""
        get() = field
        set(value) {
            field = value
        }

    var name: String = ""
        get() = field
        set(value) {
            field = value
        }

    var profileImageUrl: String = ""
        get() = field
        set(value) {
            field = value
        }

    var age: String = ""
        get() = field
        set(value) {
            field = value
        }

    constructor(userId: String, name: String, profileImageUrl: String, age: String) {
        this.userId = userId
        this.name = name
        this.profileImageUrl = profileImageUrl
        this.age = age
    }
}