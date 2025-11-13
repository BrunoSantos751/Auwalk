package backend.auwalk.controller

import backend.auwalk.service.AuthService
import org.springframework.web.bind.annotation.*

data class LoginRequest(val email: String, val senha: String)
data class LoginResponse(val success: Boolean, val token: String?)

@RestController
@RequestMapping("/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): LoginResponse {
        val token = authService.autenticar(request.email, request.senha)
        return if (token != null) {
            LoginResponse(true, token)
        } else {
            LoginResponse(false, null)
        }
    }
}
