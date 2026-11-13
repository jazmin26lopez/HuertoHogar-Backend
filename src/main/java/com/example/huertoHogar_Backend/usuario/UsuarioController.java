
package com.example.huertoHogar_Backend.usuario;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
@PreAuthorize("hasRole('Administrador')")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping
    public List<UsuarioService.UsuarioRespuesta> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public UsuarioService.UsuarioRespuesta buscar(
            @PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<UsuarioService.UsuarioRespuesta> crear(
            @RequestBody UsuarioService.UsuarioEntrada datos) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.crear(datos));
    }

    @PutMapping("/{id}")
    public UsuarioService.UsuarioRespuesta actualizar(
            @PathVariable Long id,
            @RequestBody UsuarioService.UsuarioEntrada datos) {
        return service.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(
            @PathVariable Long id) {
        service.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
