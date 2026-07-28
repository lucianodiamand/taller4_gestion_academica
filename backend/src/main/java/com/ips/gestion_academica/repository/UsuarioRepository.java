package com.ips.gestion_academica.repository;

import com.ips.gestion_academica.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}