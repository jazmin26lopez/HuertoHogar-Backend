package com.example.huertoHogar_Backend.auth;

import com.example.huertoHogar_Backend.auth.AuthService.LoginSolicitud;
import com.example.huertoHogar_Backend.auth.AuthService.RegistroSolicitud;
import com.example.huertoHogar_Backend.auth.AuthService.RespuestaAuth;
import com.example.huertoHogar_Backend.seguridad.UsuarioAutenticado;
import com.example.huertoHogar_Backend.usuario.UsuarioService.UsuarioRespuesta;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticacion", description = "Registro, inicio de sesion y renovacion de token (JWT)")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @Operation(summary = "Iniciar sesion",
            description = "Entrega un token JWT. Usalo en el boton Authorize de Swagger o en la cabecera Authorization: Bearer <token>.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inicio de sesion correcto: entrega el token JWT y los datos del usuario"),
            @ApiResponse(responseCode = "400", description = "Falta el correo o la contrasena"),
            @ApiResponse(responseCode = "401", description = "Credenciales invalidas (correo inexistente, contrasena incorrecta o usuario desactivado)")
    })
    @PostMapping("/login")
    public RespuestaAuth login(@Valid @RequestBody LoginSolicitud datos) {
        return service.login(datos);
    }

    @Operation(summary = "Registrar un cliente",
            description = "Crea una cuenta con rol Cliente y entrega su token. El rol no se puede elegir.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cuenta creada con rol Cliente; entrega el token JWT"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos (RUN, correo, contrasena de 8 a 72 caracteres, etc.)"),
            @ApiResponse(responseCode = "409", description = "El RUN o el correo ya estan registrados")
    })
    @PostMapping("/registro")
    public ResponseEntity<RespuestaAuth> registro(
            @RequestBody RegistroSolicitud datos) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.registrar(datos));
    }

    @Operation(summary = "Datos del usuario logueado",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Datos del usuario logueado"),
            @ApiResponse(responseCode = "401", description = "Sin sesion o token invalido")
    })
    @GetMapping("/me")
    public UsuarioRespuesta me(
            @AuthenticationPrincipal UsuarioAutenticado actual) {
        return service.perfil(actual.usuarioId());
    }

    @Operation(summary = "Renovar el token",
            description = "Con un token todavia valido entrega uno nuevo.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token nuevo"),
            @ApiResponse(responseCode = "401", description = "Sin sesion, token invalido o usuario desactivado")
    })
    @PostMapping("/refresh")
    public RespuestaAuth refresh(
            @AuthenticationPrincipal UsuarioAutenticado actual) {
        return service.renovar(actual.usuarioId());
    }
}
