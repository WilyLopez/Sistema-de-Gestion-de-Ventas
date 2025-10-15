package com.sgvi.sistema_ventas.repository;

import com.sgvi.sistema_ventas.model.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository para la entidad Categoria.
 * Permite la gestión de categorías de productos.
 */
@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {
    // Se pueden añadir métodos para buscar por nombre único si aplica
}