package com.sgvi.sistema_ventas.util.converter;

import com.sgvi.sistema_ventas.model.enums.TipoDocumento;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TipoDocumentoConverter implements AttributeConverter<TipoDocumento, String> {

    @Override
    public String convertToDatabaseColumn(TipoDocumento tipoDocumento) {
        if (tipoDocumento == null) {
            return null;
        }
        return tipoDocumento.getCodigo();
    }

    @Override
    public TipoDocumento convertToEntityAttribute(String codigo) {
        if (codigo == null) {
            return null;
        }
        return TipoDocumento.fromCodigo(codigo);
    }
}