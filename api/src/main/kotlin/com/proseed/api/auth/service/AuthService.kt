package com.proseed.api.auth.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.proseed.api.auth.dto.AuthRegisterRequest
import com.proseed.api.auth.dto.AuthRequest
import com.proseed.api.auth.dto.AuthResponse
import com.proseed.api.auth.dto.kakao.KakaoLoginPageResponse
import com.proseed.api.auth.dto.kakao.KakaoTokenRequest
import com.proseed.api.auth.dto.kakao.KakaoTokenResponse
import com.proseed.api.auth.dto.kakao.KakaoValueBuilder
import com.proseed.api.config.exception.auth.kakao.KakaoAuthorizationCodeNotFoundException
import com.proseed.api.config.jwt.JwtService
import com.proseed.api.user.exception.UserNotFoundException
import com.proseed.api.user.model.Role
import com.proseed.api.user.model.User
import com.proseed.api.user.repository.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: BCryptPasswordEncoder,
    private val jwtService: JwtService,
    private val authenticationManager: AuthenticationManager,
    private val kakaoValueBuilder: KakaoValueBuilder,
    private val restTemplate: RestTemplate,
    private val objectMapper: ObjectMapper
) {
    fun register(request: AuthRegisterRequest): AuthResponse {
        var user = User(
            nickName = request.nickName,
            email = request.email,
            platformId = request.platformId,
            platformType = passwordEncoder.encode(request.platformType),
            role = Role.USER)

        userRepository.save(user)
        var jwtToken = jwtService.generateToken(user)

        return AuthResponse(jwtToken)
    }

    fun authenticate(request: AuthRequest): AuthResponse {
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                request.platformId,
                request.platformType
            )
        )
        var user = userRepository.findByPlatformId(request.platformId) ?: throw UserNotFoundException()
        var jwtToken = jwtService.generateToken(user)

        return AuthResponse(jwtToken)
    }

    fun kakaoLoginPage(): KakaoLoginPageResponse {
        return kakaoValueBuilder.kakaoLoginPageResponse()
    }

    // 토큰 발급 시스템
    fun kakaoTokenProvider(code: String): KakaoTokenResponse {
        // Header 설정
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

        // kakaoTokenRequest 객체 생성
        val kakaoTokenRequest = kakaoValueBuilder.kakaoTokenRequest(code)

        return restTemplate.postForObject(
            "https://kauth.kakao.com/oauth/token",
            HttpEntity(KakaoTokenRequest.of(kakaoTokenRequest), headers),
            KakaoTokenResponse::class.java
        ) ?: throw KakaoAuthorizationCodeNotFoundException()
    }
}