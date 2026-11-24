
package com.example.huertoHogar_Backend.boleta;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DetalleBoletaRepository
        extends JpaRepository<DetalleBoletaModel, Long> {

    List<DetalleBoletaModel> findByBoletaId(Long boletaId);
}
