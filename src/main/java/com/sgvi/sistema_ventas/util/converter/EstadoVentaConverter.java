package com.sgvi.sistema_ventas.util.converter;

import com.sgvi.sistema_ventas.model.enums.EstadoVenta;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EstadoVentaConverter implements AttributeConverter<EstadoVenta, String> {

    @Override
    public String convertToDatabaseColumn(EstadoVenta estado) {
        if (estado == null) {
            return null;
        }
        return estado.getValor();

    }
    @Override
    public EstadoVenta convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return EstadoVenta.fromValor(dbData);
    }
}