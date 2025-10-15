package com.sgvi.sistema_ventas.repository;

import com.sgvi.sistema_ventas.model.entity.MetodoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository para la entidad MetodoPago.
 * Proporciona acceso a los métodos de pago aceptados en la tienda (RF-007).
 */
@Repository
public interface MetodoPagoRepository extends JpaRepository<MetodoPago, Long> {
    // Se pueden añadir métodos de búsqueda por nombre si se requiere verificar unicidad
}
