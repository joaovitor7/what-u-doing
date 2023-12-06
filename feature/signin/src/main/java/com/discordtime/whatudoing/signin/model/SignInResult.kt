package com.discordtime.whatudoing.signin.model

//TODO: Refactor it to support generic types a not use this null approach
data class SignInResult (
    val data: User?,
    val errorMsg: String?
)
data class User (
    val id: String,
    val name: String?,
    val pictureUrl: String?,
)