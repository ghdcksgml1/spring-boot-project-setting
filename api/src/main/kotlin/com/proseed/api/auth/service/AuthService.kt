package com.proseed.api.auth.service

import com.proseed.api.auth.dto.AuthRegisterRequest
import com.proseed.api.auth.dto.AuthRequest
import com.proseed.api.auth.dto.AuthResponse
import com.proseed.api.auth.dto.kakao.*
import com.proseed.api.config.exception.auth.kakao.KakaoAuthorizationCodeNotFoundException
import com.proseed.api.config.exception.auth.kakao.KakaoTokenExpiredException
import com.proseed.api.config.jwt.JwtService
import com.proseed.api.config.exception.user.UserNotFoundException
import com.proseed.api.user.model.Role
import com.proseed.api.user.model.User
import com.proseed.api.user.repository.UserRepository
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
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
    private val restTemplate: RestTemplate
) {
    fun register(request: AuthRegisterRequest): AuthResponse {
        var user = User(
            nickName = request.nickName,
            email = request.email,
            platformId = passwordEncoder.encode(request.platformId),
            platformType = request.platformType,
            profileImageUrl = "",
            role = Role.USER)

        val savedUser = userRepository.save(user)
        var jwtToken = jwtService.generateToken(savedUser)

        return AuthResponse(jwtToken)
    }

    fun authenticate(request: AuthRequest): AuthResponse {
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                request.email,
                request.platformId
            )
        )
        var user = userRepository.findByEmail(request.email) ?: throw UserNotFoundException()

        if(!passwordEncoder.matches(request.platformId, user.platformId))
            throw UserNotFoundException()

        var jwtToken = jwtService.generateToken(user)

        return AuthResponse(jwtToken)
    }

    fun kakaoLoginPage(): KakaoLoginPageResponse {
        return kakaoValueBuilder.kakaoLoginPageResponse()
    }

    // ?????? ?????? ?????????
    fun kakaoTokenProvider(code: String): AuthResponse {
        // Header ??????
        val tokenRequestHeader = HttpHeaders()
        tokenRequestHeader.contentType = MediaType.APPLICATION_FORM_URLENCODED

        // kakaoTokenRequest ?????? ??????
        val kakaoTokenRequest = kakaoValueBuilder.kakaoTokenRequest(code)

        val kakaoTokenResponse = restTemplate.postForObject(
            "https://kauth.kakao.com/oauth/token",
            HttpEntity(KakaoTokenRequest.of(kakaoTokenRequest), tokenRequestHeader),
            KakaoTokenResponse::class.java
        ) ?: throw KakaoAuthorizationCodeNotFoundException()

        // Header ??????
        val userInfoHeader = HttpHeaders()
        userInfoHeader.set("Authorization", "Bearer " + kakaoTokenResponse.access_token)

        val entity = HttpEntity("", userInfoHeader)

        val kakaoUserInfo = restTemplate.exchange(
            "https://kapi.kakao.com/v2/user/me",
            HttpMethod.GET,
            entity,
            KakaoUserInfoResponse::class.java
        ).body ?: throw KakaoTokenExpiredException()

        var email = kakaoUserInfo.kakao_account.email

        // User ??????????????? platformId??? ?????????.
        // ?????? ?????? ??????????????? user????????? ???????????? jwt?????? ??????
        // user ????????? ???????????? ????????????, user????????? ???????????? jwt?????? ??????

        var user = userRepository.findByEmail(email)

        if (user == null) { // ????????????
            user = userRepository.save(
                User(
                    nickName = kakaoUserInfo.properties.nickname,
                    email = email,
                    platformId = passwordEncoder.encode(kakaoUserInfo.id),
                    platformType = "KAKAO",
                    profileImageUrl = kakaoUserInfo.properties.profile_image,
                    role = Role.USER
                ))
        } else  { // ?????????, ????????? ?????? ????????????
            user.copy(
                nickName = kakaoUserInfo.properties.nickname,
                profileImageUrl = kakaoUserInfo.properties.profile_image
            )
            user = userRepository.save(user)
        }

        val jwtToken = jwtService.generateToken(user)

        return AuthResponse(jwtToken)
    }

}
