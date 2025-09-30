package backend.auwalk.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import java.util.*

object JwtUtil {
    private const val EXPIRATION_TIME = 1000 * 60 * 60 // 1 hora
    private val SECRET_KEY = Keys.hmacShaKeyFor("MinhaChaveSuperSecretaDe32Caracteres!".toByteArray())

    fun generateToken(email: String): String {
        val now = Date()
        val expiration = Date(now.time + EXPIRATION_TIME)

        return Jwts.builder()
            .setSubject(email)
            .setIssuedAt(now)
            .setExpiration(expiration)
            .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
            .compact()
    }

    fun validateToken(token: String): String? {
        return try {
            val claims = Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
            claims.body.subject // retorna o email
        } catch (e: Exception) {
            null
        }
    }
}
