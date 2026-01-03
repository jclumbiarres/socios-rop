package net.lumbi.socios.mapper;

import net.lumbi.socios.domain.SocioEntity;
import net.lumbi.socios.dto.SocioDTO;

public class SocioMapper {

    public static SocioEntity toEntity(SocioDTO dto) {
        if (dto == null)
            return null;
        return SocioEntity.builder()
                .nombre(dto.nombre())
                .dni(dto.dni())
                .numero(dto.numero())
                .fechaNacimiento(dto.fechaNacimiento())
                .build();
    }

    public static SocioDTO toDTO(SocioEntity entity) {
        if (entity == null)
            return null;
        return new SocioDTO(
                entity.getNombre(),
                entity.getDni(),
                entity.getNumero(),
                entity.getFechaNacimiento());
    }
}