package com.sgvi.sistema_ventas.util.converter;

import com.sgvi.sistema_ventas.model.enums.TipoAlerta;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TipoAlertaConverter implements AttributeConverter<TipoAlerta, String> {

    @Override
    public String convertToDatabaseColumn(TipoAlerta attribute) {
        return attribute != null ? attribute.getValor() : null;
    }

    @Override
    public TipoAlerta convertToEntityAttribute(String dbData) {
        return dbData != null ? TipoAlerta.fromValor(dbData) : null;
    }
}