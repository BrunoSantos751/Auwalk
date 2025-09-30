package backend.auwalk.service

import backend.auwalk.security.JwtUtil
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val jdbcTemplate: JdbcTemplate
) {

    fun autenticar(email: String, senha: String): String? {
        val sql = "SELECT senha FROM usuario WHERE email = ?"
        val senhaBanco = try {
            jdbcTemplate.queryForObject(sql, arrayOf(email), String::class.java)
        } catch (e: Exception) {
            return null // email não encontrado
        }

        return if (senhaBanco == senha) {
            JwtUtil.generateToken(email) // retorna token JWT
        } else {
            null
        }
    }
}
