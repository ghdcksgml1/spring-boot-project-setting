package com.proseed.api.auth.service

import com.proseed.api.auth.dto.AuthRegisterRequest
import com.proseed.api.auth.dto.AuthRequest
import com.proseed.api.auth.dto.AuthResponse
import com.proseed.api.config.jwt.JwtService
import com.proseed.api.user.model.Role
import com.proseed.api.user.model.User
import com.proseed.api.user.repository.UserRepository
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
    private val authenticationManager: AuthenticationManager
) {
    fun register(request: AuthRegisterRequest): AuthResponse {
        var user = User(
            firstname = request.firstname,
            lastname = request.lastname,
            email = request.email,
            _password = passwordEncoder.encode(request.password),
            role = Role.USER)

        userRepository.save(user)
        var jwtToken = jwtService.generateToken(user)

        return AuthResponse(jwtToken)
    }

    fun authenticate(request: AuthRequest): AuthResponse {
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                request.email,
                request.password
            )
        )
        var user = userRepository.findByEmail(request.email) ?: throw UsernameNotFoundException("User Email is not correct")
        var jwtToken = jwtService.generateToken(user)

        return AuthResponse(jwtToken)
    }
}