package net.lumbi.socios.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
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

        return detectConstraintViolation(msg)
                .map(field -> mapToSocioError(field, dto))
                .orElseThrow(() -> e);
    }

    private Optional<String> detectConstraintViolation(String errorMessage) {
        Map<String, List<String>> fieldPatterns = Map.of(
                "dni", List.of("dni", "uk_socio_dni"),
                "numero", List.of("numero", "uk_socio_numero"),
                "nombre", List.of("nombre", "uk_socio_nombre"));

        return fieldPatterns.entrySet().stream()
                .filter(entry -> entry.getValue().stream()
                        .anyMatch(errorMessage::contains))
                .map(Map.Entry::getKey)
                .findFirst();
    }

    private Result<SocioEntity, SocioError> mapToSocioError(String field, SocioDTO dto) {
        return switch (field) {
            case "dni" -> Result.failure(new SocioError.DNIAlreadyExists(dto.dni()));
            case "numero" -> Result.failure(new SocioError.NumeroAlreadyExists(dto.numero()));
            case "nombre" -> Result.failure(new SocioError.NombreAlreadyExists(dto.nombre()));
            default -> throw new IllegalStateException("Campo no reconocido: " + field);
        };
    }

    private Result<SocioDTO, SocioError> validateInexistence(SocioDTO dto) {
        String conflictingField = socioRepository.findFirstConflictingField(
                dto.dni(),
                dto.numero(),
                dto.nombre());

        if (conflictingField == null) {
            return Result.success(dto);
        }
        return mapToSocioError(conflictingField, dto)
                .map(entity -> dto);
    }

}
