
package com.example.huertoHogar_Backend.categoria;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository
        extends JpaRepository<CategoriaModel, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndCategoriaIdNot(
            String nombre, Long categoriaId);
}
