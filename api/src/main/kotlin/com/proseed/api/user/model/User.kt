package com.proseed.api.user.model

import jakarta.persistence.*
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

@Entity
@Table(name = "_user")
data class User(
    @Id @GeneratedValue
    val id: Int? = null,
    val firstname: String,
    val lastname: String,
    val email: String,
    val _password: String,
    @Enumerated(EnumType.STRING)
    val role: Role
) : UserDetails {

    constructor() : this(null,"","","","",Role.USER) // NoArgsConstructor

    // UserDetails Implements
    override fun getAuthorities(): List<SimpleGrantedAuthority> {
        return listOf(SimpleGrantedAuthority(role.name))
    }

    override fun getPassword(): String {
        return _password
    }

    override fun getUsername(): String {
        return email
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        return true
    }
    // ====

    override fun toString(): String {
        return "User(id=$id, firstname='$firstname', lastname='$lastname', email='$email', _password='$_password', role=$role)"
    }
}