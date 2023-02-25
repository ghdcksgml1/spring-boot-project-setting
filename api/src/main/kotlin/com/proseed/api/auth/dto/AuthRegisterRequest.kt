package com.proseed.api.auth.dto

data class AuthRegisterRequest(
    var firstname: String,
    var lastname: String,
    var email: String,
    var password: String
)