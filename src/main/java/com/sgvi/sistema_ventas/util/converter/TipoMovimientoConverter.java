package com.sgvi.sistema_ventas.util.converter;

import com.sgvi.sistema_ventas.model.enums.TipoMovimiento;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TipoMovimientoConverter implements AttributeConverter<TipoMovimiento, String> {

    @Override
    public String convertToDatabaseColumn(TipoMovimiento tipo) {
        if (tipo == null) {
            return null;
        }
        return tipo.getValor();
    }

    @Override
    public TipoMovimiento convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return TipoMovimiento.fromValor(dbData);
    }
}
