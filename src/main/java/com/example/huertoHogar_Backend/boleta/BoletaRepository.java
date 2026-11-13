
package com.example.huertoHogar_Backend.boleta;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BoletaRepository
        extends JpaRepository<BoletaModel, Long> {

    List<BoletaModel> findByUsuarioId(Long usuarioId);
}
