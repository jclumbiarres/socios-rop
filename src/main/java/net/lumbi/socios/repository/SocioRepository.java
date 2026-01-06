package net.lumbi.socios.repository;

import org.springframework.stereotype.Repository;

import jakarta.persistence.Table;
import net.lumbi.socios.domain.SocioEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

@Repository
@Table(name = "socios")
public interface SocioRepository extends JpaRepository<SocioEntity, Long> {
    SocioEntity findByDniAndIdNot(String dni, Long idSocio);

    SocioEntity findByDni(String dni);

    SocioEntity findByNumero(Integer numero);

    SocioEntity findByNombre(String nombre);

    boolean existsByDni(String dni);

    boolean existsByNumero(Integer numero);

    boolean existsByNombre(String nombre);

    boolean existsByDniOrNombreOrNumero(String dni, String nombre, Integer numero);

    @Query(value = """
            SELECT CASE
                WHEN EXISTS (SELECT 1 FROM socios WHERE dni = :dni) THEN 'dni'
                WHEN EXISTS (SELECT 1 FROM socios WHERE numero = :numero) THEN 'numero'
                WHEN EXISTS (SELECT 1 FROM socios WHERE nombre = :nombre) THEN 'nombre'
                ELSE NULL
            END
            """, nativeQuery = true)
    String findFirstConflictingField(String dni, Integer numero, String nombre);
}
