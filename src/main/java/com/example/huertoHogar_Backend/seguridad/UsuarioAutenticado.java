package com.example.huertoHogar_Backend.seguridad;

/**
 * Datos del usuario que hizo la peticion, ya validado por el token.
 * Se puede recibir en un controlador con @AuthenticationPrincipal.
 */
public record UsuarioAutenticado(Long usuarioId, String correo, String rol) {
}
