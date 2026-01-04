package net.lumbi.socios.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import net.lumbi.socios.domain.error.SocioError;
import net.lumbi.socios.dto.ResponseDTO;
import net.lumbi.socios.dto.SocioDTO;
import net.lumbi.socios.service.SocioService;

@RestController
@RequestMapping("/api/v1/socios")
public class SocioController {
    private final SocioService socioService;

    public SocioController(SocioService socioService) {
        this.socioService = socioService;
    }

    // DEBUG: se usa para comprobar que los virtual threads funcionan
    @GetMapping("/thread")
    public String getThreadType() {
        return Thread.currentThread().toString();
    }

    @PostMapping("/add")
    public ResponseEntity<ResponseDTO> createSocio(@Valid @RequestBody SocioDTO socio) {
        return socioService.createSocio(socio).fold(
                savedDto -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(new ResponseDTO(
                                HttpStatus.CREATED.value(),
                                "Socio creado exitosamente: " + savedDto.dni())),
                this::errorResponse);
    }

    private ResponseEntity<ResponseDTO> errorResponse(SocioError error) {
        HttpStatus status = errorType(error);
        return ResponseEntity.status(status)
                .body(new ResponseDTO(status.value(), errorMsg(error)));
    }

    private HttpStatus errorType(SocioError err) {
        return switch (err) {
            case SocioError.DNIAlreadyExists e -> HttpStatus.CONFLICT;
            case SocioError.NumeroAlreadyExists e -> HttpStatus.CONFLICT;
            case SocioError.NombreAlreadyExists e -> HttpStatus.CONFLICT;
            case SocioError.EmptyField e -> HttpStatus.BAD_REQUEST;
            case SocioError.DatabaseBoom e -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    private String errorMsg(SocioError err) {
        return switch (err) {
            case SocioError.DNIAlreadyExists e -> "Ya existe un socio con DNI: " + e.dni();
            case SocioError.EmptyField e ->
                "No se puede crear el socio porque hay campos vacíos " + e.field();
            case SocioError.NumeroAlreadyExists e -> "Ya existe un socio con número: " + e.numero();
            case SocioError.NombreAlreadyExists e -> "Ya existe un socio con nombre: " + e.nombre();
            case SocioError.DatabaseBoom e -> "Error crítico de base de datos";
        };
    }
}
