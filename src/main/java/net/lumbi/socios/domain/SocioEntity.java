package net.lumbi.socios.domain;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "socios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String nombre;

    @NotNull
    @Column(nullable = false, unique = true)
    private String dni;

    @NotNull
    @Column(nullable = false)
    private LocalDate fechaNacimiento;

    @NotNull
    @Column(nullable = false)
    private Integer numero;

}
