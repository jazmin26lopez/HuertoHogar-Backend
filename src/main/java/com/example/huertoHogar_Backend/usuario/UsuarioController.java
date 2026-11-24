
package com.example.huertoHogar_Backend.usuario;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
@PreAuthorize("hasRole('Administrador')")
@Tag(
        name = "Usuarios",
        description = "API para administrar usuarios de HuertoHogar. " +
                "Todas las operaciones requieren rol Administrador."
)
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @Operation(
            summary = "Listar usuarios",
            description = "Obtiene los usuarios registrados, sin exponer sus contraseñas."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Listado de usuarios obtenido correctamente"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuario no autenticado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Permisos insuficientes",
                    content = @Content
            )
    })
    @GetMapping
    public List<UsuarioService.UsuarioRespuesta> listar() {
        return service.listar();
    }

    @Operation(
            summary = "Buscar usuario por ID",
            description = "Consulta los datos de un usuario registrado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario encontrado"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuario no autenticado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Permisos insuficientes",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado",
                    content = @Content
            )
    })
    @GetMapping("/{id}")
    public UsuarioService.UsuarioRespuesta buscar(
            @Parameter(
                    description = "ID del usuario",
                    example = "1"
            )
            @PathVariable Long id) {

        return service.buscar(id);
    }

    @Operation(
            summary = "Crear usuario",
            description = "Registra un usuario con un rol permitido. " +
                    "La contraseña se almacena mediante BCrypt. " +
                    "Solo Administrador puede utilizar esta operación."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Usuario creado correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de usuario inválidos",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuario no autenticado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Permisos insuficientes",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "RUN o correo ya registrado",
                    content = @Content
            )
    })
    @PostMapping
    public ResponseEntity<UsuarioService.UsuarioRespuesta> crear(
            @RequestBody UsuarioService.UsuarioEntrada datos) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.crear(datos));
    }

    @Operation(
            summary = "Actualizar usuario",
            description = "Modifica los datos de un usuario existente. " +
                    "Solo Administrador puede realizar esta operación."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario actualizado correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuario no autenticado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Permisos insuficientes",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Correo ya registrado",
                    content = @Content
            )
    })
    @PutMapping("/{id}")
    public UsuarioService.UsuarioRespuesta actualizar(
            @Parameter(
                    description = "ID del usuario",
                    example = "1"
            )
            @PathVariable Long id,
            @RequestBody UsuarioService.UsuarioEntrada datos) {

        return service.actualizar(id, datos);
    }

    @Operation(
            summary = "Desactivar usuario",
            description = "Realiza una desactivación lógica. " +
                    "Solo Administrador puede utilizar esta operación."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Usuario desactivado correctamente"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuario no autenticado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Permisos insuficientes",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado",
                    content = @Content
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(
            @Parameter(
                    description = "ID del usuario",
                    example = "1"
            )
            @PathVariable Long id) {

        service.desactivar(id);

        return ResponseEntity.noContent().build();
    }
}
