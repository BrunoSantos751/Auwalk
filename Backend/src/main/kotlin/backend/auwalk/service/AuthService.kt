package backend.auwalk.service

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val jdbcTemplate: JdbcTemplate
) {

    fun autenticar(email: String, senha: String): Boolean {
        val sql = "SELECT senha FROM usuario WHERE email = ?"
        val senhaBanco = try {
            jdbcTemplate.queryForObject(sql, arrayOf(email), String::class.java)
        } catch (e: Exception) {
            return false // email não encontrado
        }

        return senhaBanco == senha
    }
}