package com.sgvi.sistema_ventas.util.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import com.sgvi.sistema_ventas.model.enums.NivelUrgencia;

@Converter(autoApply = true)
public class NivelUrgenciaConverter implements AttributeConverter<NivelUrgencia, String> {

    @Override
    public String convertToDatabaseColumn(NivelUrgencia nivel) {
        return nivel != null ? nivel.getValor() : null; // Guardar el valor en minúscula
    }

    @Override
    public NivelUrgencia convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            return NivelUrgencia.fromValor(dbData); // Usa tu método que es case-insensitive
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Valor de NivelUrgencia inválido en DB: " + dbData, e);
        }
    }
}

