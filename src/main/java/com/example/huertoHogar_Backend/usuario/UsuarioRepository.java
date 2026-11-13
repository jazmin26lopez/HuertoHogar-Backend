
package com.example.huertoHogar_Backend.usuario;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository
        extends JpaRepository<UsuarioModel, Long> {

    Optional<UsuarioModel> findByCorreoIgnoreCase(String correo);

    boolean existsByCorreoIgnoreCase(String correo);

    boolean existsByRun(String run);

    boolean existsByCorreoIgnoreCaseAndUsuarioIdNot(
            String correo, Long usuarioId);

    boolean existsByRunAndUsuarioIdNot(
            String run, Long usuarioId);
}
