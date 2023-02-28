package com.proseed.api.auth.dto

data class AuthRequest(
    val platformId: String,
    val platformType: String
)