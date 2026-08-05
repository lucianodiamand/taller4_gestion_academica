package com.ips.gestion_academica.repository;

import com.ips.gestion_academica.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    boolean existsByEmail(String email);

    boolean existsByDni(String dni);

    boolean existsByLegajo(String legajo);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByDniAndIdNot(String dni, Long id);

    boolean existsByLegajoAndIdNot(String legajo, Long id);

    List<Usuario> findByActivoTrue();

    Optional<Usuario> findByLegajo(String legajo);

    Optional<Usuario> findByDni(String dni);

}