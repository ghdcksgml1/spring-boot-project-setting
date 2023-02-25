package com.proseed.api.auth.dto

data class AuthRequest(
    var email: String,
    var password: String
)