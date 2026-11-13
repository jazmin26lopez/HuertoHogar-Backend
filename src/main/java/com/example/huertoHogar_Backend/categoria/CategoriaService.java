package com.example.huertoHogar_Backend.categoria;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository repository;

    public CategoriaService(CategoriaRepository repository) {
        this.repository = repository;
    }

    public List<CategoriaModel> listarCategorias() {
        return repository.findAll();
    }
}