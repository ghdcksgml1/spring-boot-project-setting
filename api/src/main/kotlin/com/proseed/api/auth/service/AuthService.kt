package com.proseed.api.auth.service

import com.proseed.api.auth.dto.AuthRegisterRequest
import com.proseed.api.auth.dto.AuthRequest
import com.proseed.api.auth.dto.AuthResponse
import com.proseed.api.auth.dto.kakao.KakaoLoginPageResponse
import com.proseed.api.config.jwt.JwtService
import com.proseed.api.user.exception.UserNotFoundException
import com.proseed.api.user.model.Role
import com.proseed.api.user.model.User
import com.proseed.api.user.repository.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: BCryptPasswordEncoder,
    private val jwtService: JwtService,
    private val authenticationManager: AuthenticationManager,
    @Value("\${spring.security.oauth2.client.provider.authorization-uri}") var authorizationUri: String,
    @Value("\${spring.security.oauth2.client.provider.token-uri}") var tokenUri: String,
    @Value("\${spring.security.oauth2.client.provider.user-info-uri}") var userInfoUri: String,
    @Value("\${spring.security.oauth2.client.kakao.client-id}") var clientId: String,
    @Value("\${spring.security.oauth2.client.kakao.client-secret}") var clientSecret: String,
    @Value("\${spring.security.oauth2.client.kakao.redirect-uri}") var redirectUri: String,
    @Value("\${spring.security.oauth2.client.kakao.scope}") var scope: String,
    @Value("\${spring.security.oauth2.client.kakao.nonce}") var nonce: String
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
        return KakaoLoginPageResponse(
            loginPage = "${authorizationUri}?" +
                    "response_type=code&" +
                    "client_id=${clientId}&" +
                    "redirect_uri=${redirectUri}&" +
                    "client_secret=${clientSecret}&" +
                    "scope=${scope}&" +
                    "nonce=${nonce}"
        )
    }
}