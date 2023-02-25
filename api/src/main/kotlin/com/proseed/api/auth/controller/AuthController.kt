package com.proseed.api.auth.controller

import com.proseed.api.auth.dto.AuthRegisterRequest
import com.proseed.api.auth.dto.AuthRequest
import com.proseed.api.auth.dto.AuthResponse
import com.proseed.api.auth.service.AuthService
import com.proseed.api.user.exception.UserNotFoundException
import com.proseed.api.user.model.User
import com.proseed.api.user.repository.UserRepository
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService,
    private val userRepository: UserRepository
) {

    @PostMapping("/register")
    fun register(
        @RequestBody request: AuthRegisterRequest
    ): ResponseEntity<AuthResponse> {

        return ResponseEntity.ok(authService.register(request))
    }

    @PostMapping("/authenticate")
    fun authenticate(
        @RequestBody request: AuthRequest
    ): ResponseEntity<AuthResponse> {

        return ResponseEntity.ok(authService.authenticate(request))
    }

    @GetMapping("/valid")
    fun isValidToken(@AuthenticationPrincipal user: User): Boolean {
        return user != null
    }

    @PostMapping("/test")
    fun errorTest(@RequestBody request: AuthRequest): ResponseEntity<AuthResponse> {
        var user = userRepository.findByEmail(request.email) ?: throw UserNotFoundException()
        return ResponseEntity.ok(AuthResponse("ok"))
    }
}