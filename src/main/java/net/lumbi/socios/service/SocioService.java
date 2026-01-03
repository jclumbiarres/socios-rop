package net.lumbi.socios.service;

import java.util.Set;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import net.lumbi.socios.domain.SocioEntity;
import net.lumbi.socios.domain.error.SocioError;
import net.lumbi.socios.dto.SocioDTO;
import net.lumbi.socios.kernel.Result;
import net.lumbi.socios.mapper.SocioMapper;
import net.lumbi.socios.repository.SocioRepository;

@Service
public class SocioService {

    private final SocioRepository socioRepository;

    public SocioService(SocioRepository socioRepository) {
        this.socioRepository = socioRepository;
    }

    @Transactional
    public Result<SocioDTO, SocioError> createSocio(SocioDTO socioDto) {
        return validateInexistence(socioDto) // Success(SocioDTO)
                .flatMap(this::persist) // Success(SocioEntity)
                .map(SocioMapper::toDTO); // Success(SocioDTO)
    }

    private Result<SocioEntity, SocioError> persist(SocioDTO dto) {
        try {
            SocioEntity entity = SocioMapper.toEntity(dto);

            // Validación Bean Validation antes de persistir
            Set<ConstraintViolation<SocioEntity>> violations = Validation
                    .buildDefaultValidatorFactory()
                    .getValidator()
                    .validate(entity);

            if (!violations.isEmpty()) {
                // Tomamos el primer campo que falla para simplificar
                ConstraintViolation<SocioEntity> violation = violations.iterator().next();
                String fieldName = violation.getPropertyPath().toString();
                return Result.failure(new SocioError.EmptyField(fieldName));
            }

            SocioEntity saved = socioRepository.save(entity);
            return Result.success(saved);

        } catch (DataIntegrityViolationException e) {
            return parseDatabaseError(e, dto);
        }
    }

    private Result<SocioEntity, SocioError> parseDatabaseError(
            DataIntegrityViolationException e,
            SocioDTO dto) {

        String msg = e.getMostSpecificCause().getMessage().toLowerCase();

        String field = null;
        if (msg.contains("dni") || msg.contains("uk_socio_dni"))
            field = "dni";
        else if (msg.contains("numero") || msg.contains("uk_socio_numero"))
            field = "numero";
        else if (msg.contains("nombre") || msg.contains("uk_socio_nombre"))
            field = "nombre";

        return switch (field) {
            case "dni" -> Result.failure(new SocioError.DNIAlreadyExists(dto.dni()));
            case "numero" -> Result.failure(new SocioError.NumeroAlreadyExists(dto.numero()));
            case "nombre" -> Result.failure(new SocioError.NombreAlreadyExists(dto.nombre()));
            case null -> Result.failure(new SocioError.EmptyField("NULL"));
            default -> throw e;
        };
    }

    private Result<SocioDTO, SocioError> validateInexistence(SocioDTO dto) {
        return checkDni(dto)
                .flatMap(this::checkNumero)
                .flatMap(this::checkNombre);
    }

    private Result<SocioDTO, SocioError> checkDni(SocioDTO dto) {
        return socioRepository.existsByDni(dto.dni())
                ? Result.failure(new SocioError.DNIAlreadyExists(dto.dni()))
                : Result.success(dto);
    }

    private Result<SocioDTO, SocioError> checkNumero(SocioDTO dto) {
        return socioRepository.existsByNumero(dto.numero())
                ? Result.failure(new SocioError.NumeroAlreadyExists(dto.numero()))
                : Result.success(dto);
    }

    private Result<SocioDTO, SocioError> checkNombre(SocioDTO dto) {
        return socioRepository.existsByNombre(dto.nombre())
                ? Result.failure(new SocioError.NombreAlreadyExists(dto.nombre()))
                : Result.success(dto);
    }
}
