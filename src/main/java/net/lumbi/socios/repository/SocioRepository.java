package net.lumbi.socios.repository;

import org.springframework.stereotype.Repository;

import net.lumbi.socios.domain.SocioEntity;

import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface SocioRepository extends JpaRepository<SocioEntity, Long> {
    SocioEntity findByDniAndIdNot(String dni, Long idSocio);

    SocioEntity findByDni(String dni);

    SocioEntity findByNumero(Integer numero);

    SocioEntity findByNombre(String nombre);

    boolean existsByDni(String dni);

    boolean existsByNumero(Integer numero);

    boolean existsByNombre(String nombre);

    boolean existsByDniOrNombreOrNumero(String dni, String nombre, Integer numero);
}
