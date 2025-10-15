package com.sgvi.sistema_ventas.repository;


import com.sgvi.sistema_ventas.model.entity.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository para la entidad Proveedor.
 * Proporciona operaciones CRUD para la gestión de proveedores.
 */
@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {

    /**
     * Busca un proveedor por su RUC (Restricción Legal/Tributaria Peruana).
     * @param ruc El número de RUC a buscar.
     * @return Un Optional que contiene el Proveedor si existe.
     */
    Optional<Proveedor> findByRuc(String ruc);

    /**
     * Verifica si existe un proveedor con un RUC específico.
     * @param ruc El RUC a verificar.
     * @return true si el RUC ya existe, false en caso contrario.
     */
    boolean existsByRuc(String ruc);
}
