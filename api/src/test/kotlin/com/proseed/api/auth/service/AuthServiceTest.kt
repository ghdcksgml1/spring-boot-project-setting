package com.proseed.api.auth.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.proseed.api.auth.dto.kakao.KakaoTokenRequest
import com.proseed.api.auth.dto.kakao.KakaoTokenResponse
import com.proseed.api.auth.dto.kakao.KakaoValueBuilder
import com.proseed.api.config.exception.auth.kakao.KakaoAuthorizationCodeNotFoundException
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.web.client.RestTemplate
import java.lang.RuntimeException

@AutoConfigureMockMvc
@SpringBootTest
internal class AuthServiceTest(
    @Autowired val restTemplate: RestTemplate,
    @Autowired val kakaoValueBuilder: KakaoValueBuilder,
    @Autowired val objectMapper: ObjectMapper,
) {

    @Test
    @DisplayName(" 카카오톡 토큰 받아오기 성공 테스트")
    fun kakaoTokenProvider_PASS() {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

        val kakaoTokenRequest = kakaoValueBuilder.kakaoTokenRequest("U8f6dZyocZDpfcEHiO1x-vBNKV9pGOpIRAOow6XuuOacEWPP3VrIpOIZgYoz9Tcbos7kFAo9dNsAAAGGv4-MGg")

        val tokenResponse = restTemplate.postForObject(
            "https://kauth.kakao.com/oauth/token",
            HttpEntity(KakaoTokenRequest.of(kakaoTokenRequest), headers),
            KakaoTokenResponse::class.java
        )

        val kakaoTokenResponse = tokenResponse

        assertNotNull(kakaoTokenResponse?.access_token)
        assertNotNull(kakaoTokenResponse?.token_type)
        assertNotNull(kakaoTokenResponse?.refresh_token)
        assertNotNull(kakaoTokenResponse?.id_token)
        assertNotNull(kakaoTokenResponse?.expires_in)
        assertNotNull(kakaoTokenResponse?.scope)
        assertNotNull(kakaoTokenResponse?.refresh_token_expires_in)
    }

    @Test
    @DisplayName(" 카카오톡 토큰 받아오기 실패 테스트")
    fun kakaoTokenProvider_FAIL() {
        assertThrows(RuntimeException::class.java) {
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

            val kakaoTokenRequest = kakaoValueBuilder.kakaoTokenRequest("abcd")

            restTemplate.postForObject(
                "https://kauth.kakao.com/oauth/token",
                HttpEntity(KakaoTokenRequest.of(kakaoTokenRequest), headers),
                KakaoTokenResponse::class.java
            ) ?: throw KakaoAuthorizationCodeNotFoundException()
        }
    }
}