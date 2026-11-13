
package com.example.huertoHogar_Backend.categoria;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository repository;

    public CategoriaService(CategoriaRepository repository) {
        this.repository = repository;
    }

    // GET: listar todas las categorias
    public List<CategoriaModel> listarCategorias() {
        return repository.findAll();
    }

    // GET: buscar por ID
    public CategoriaModel buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "La categoria no existe"
                ));
    }

    // POST: crear categoria
    public CategoriaModel crearCategoria(CategoriaModel categoria) {

        validarDatos(categoria);

        String nombre = categoria.getNombre().trim();

        if (repository.existsByNombreIgnoreCase(nombre)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe una categoria con ese nombre"
            );
        }

        categoria.setCategoriaId(null);
        categoria.setNombre(nombre);

        return repository.saveAndFlush(categoria);
    }

    // PUT: actualizar categoria
    public CategoriaModel actualizarCategoria(
            Long id, CategoriaModel datos) {

        validarDatos(datos);

        CategoriaModel categoria = buscarPorId(id);
        String nombre = datos.getNombre().trim();

        if (repository.existsByNombreIgnoreCaseAndCategoriaIdNot(
                nombre, id)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe otra categoria con ese nombre"
            );
        }

        categoria.setNombre(nombre);
        categoria.setDescripcion(datos.getDescripcion());

        return repository.saveAndFlush(categoria);
    }

    // DELETE: eliminar categoria
    @Transactional
    public void eliminarCategoria(Long id) {

        CategoriaModel categoria = buscarPorId(id);

        try {
            repository.delete(categoria);
            repository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede eliminar una categoria con productos asociados",
                    e
            );
        }
    }

    private void validarDatos(CategoriaModel categoria) {

        if (categoria == null ||
                categoria.getNombre() == null ||
                categoria.getNombre().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El nombre de la categoria es obligatorio"
            );
        }

        if (categoria.getNombre().trim().length() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El nombre no puede superar 100 caracteres"
            );
        }

        if (categoria.getDescripcion() != null &&
                categoria.getDescripcion().length() > 500) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La descripcion no puede superar 500 caracteres"
            );
        }
    }
}
