
package com.example.huertoHogar_Backend.producto;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository
        extends JpaRepository<ProductoModel, Long> {

    List<ProductoModel> findByActivo(String activo);

    List<ProductoModel> findByCategoriaIdAndActivo(
            Long categoriaId, String activo);

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCaseAndProductoIdNot(
            String codigo, Long productoId);

    // Bloquear el producto durante el registro de una compra
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ProductoModel p WHERE p.productoId = :id")
    Optional<ProductoModel> bloquearPorId(@Param("id") Long id);
}
