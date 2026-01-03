package net.lumbi.socios.dto;

import java.time.LocalDate;

public record SocioDTO(String nombre, String dni, Integer numero, LocalDate fechaNacimiento) {

}
