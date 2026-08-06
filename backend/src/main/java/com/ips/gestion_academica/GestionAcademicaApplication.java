package com.ips.gestion_academica;

import com.ips.gestion_academica.model.Rol;
import com.ips.gestion_academica.model.Usuario;
import com.ips.gestion_academica.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class GestionAcademicaApplication {

	public static void main(String[] args) {
		SpringApplication.run(GestionAcademicaApplication.class, args);
	}

	@Bean
	CommandLineRunner crearAdminInicial(
			UsuarioRepository usuarioRepository,
			BCryptPasswordEncoder passwordEncoder) {

		return args -> {

			if (usuarioRepository.findByLegajo("ADMIN").isEmpty()) {

				Usuario admin = new Usuario();
				admin.setNombre("Administrador");
				admin.setApellido("Sistema");
				admin.setDni("00000000");
				admin.setEmail("admin@ips.com");
				admin.setLegajo("ADMIN");
				admin.setPassword(passwordEncoder.encode("1234"));
				admin.setRol(Rol.ADMIN);
				admin.setActivo(true);

				usuarioRepository.save(admin);

				System.out.println("======================================");
				System.out.println("ADMIN INICIAL CREADO");
				System.out.println("Legajo: ADMIN");
				System.out.println("Password: 1234");
				System.out.println("======================================");
			}

		};
	}
}