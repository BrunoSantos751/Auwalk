package backend.auwalk.controller


import org.springframework.web.bind.annotation.*
import backend.auwalk.service.AuthService

data class LoginRequest(val email: String, val senha: String)
data class LoginResponse(val authenticated: Boolean)

@RestController
@RequestMapping("/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): LoginResponse {
        val autenticado = authService.autenticar(request.email, request.senha)
        return LoginResponse(autenticado)
    }
}