package com.ips.gestion_academica.service;

import com.ips.gestion_academica.dto.curso.CursoRequest;
import com.ips.gestion_academica.dto.curso.CursoResponse;
import com.ips.gestion_academica.dto.usuario.UsuarioResumeResponse;
import com.ips.gestion_academica.exception.RecursoDuplicadoException;
import com.ips.gestion_academica.exception.RecursoNoEncontradoException;
import com.ips.gestion_academica.exception.RecursoInactivoException;
import com.ips.gestion_academica.model.Curso;
import com.ips.gestion_academica.model.Usuario;
import com.ips.gestion_academica.repository.CursoRepository;
import org.springframework.stereotype.Service;

import java.util.List;



@Service
public class CursoService {

    private final CursoRepository cursoRepository;

    public CursoService(CursoRepository cursoRepository) {
        this.cursoRepository = cursoRepository;
    }

    private UsuarioResumeResponse convertirAResume(Usuario usuario) {
        return new UsuarioResumeResponse(
            usuario.getId(),
            usuario.getNombre(),
            usuario.getApellido(),
            usuario.getLegajo(),
            usuario.getEmail()
        );
    }
    private CursoResponse convertirAResponse(Curso curso) {
        return new CursoResponse(
                curso.getId(),
                curso.getAnio(),
                curso.getCuatrimestre(),
                curso.getComision(),
                curso.getActivo(),
                convertirAResume(curso.getProfesor())
                //curso.getMateria(),
        );
    }


    public List<CursoResponse> listarCursos() {
        return cursoRepository.findByActivoTrue()
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    public CursoResponse buscarPorId(Long id) {
        Curso curso = cursoRepository.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Curso",id));

        if (!curso.getActivo()) {
            throw new RecursoInactivoException("Curso",id);
        }

        return convertirAResponse(curso);
    }


    public CursoResponse crearCurso(CursoRequest request) {
        Curso curso = new Curso();
        curso.setNombre(request.getNombre());
        curso.setApellido(request.getApellido());
        curso.setDni(request.getDni());
        curso.setEmail(request.getEmail());
        curso.setPassword(request.getPassword());
        curso.setLegajo(request.getLegajo());
        curso.setRol(request.getRol());

        curso.setActivo(true);

        if(cursoRepository.existsByEmail(curso.getEmail())){
            throw new RecursoDuplicadoException(
                "Ya existe un curso con ese email " + curso.getEmail()
            );
        }

        if(cursoRepository.existsByDni(curso.getDni())){
            throw new RecursoDuplicadoException(
                "Ya existe un curso con ese dni " + curso.getDni()
            );
        }

        if(cursoRepository.existsByLegajo(curso.getLegajo())){
            throw new RecursoDuplicadoException(
                "Ya existe un curso con ese legajo " + curso.getLegajo()
            );
        }
        Curso curso_creado = cursoRepository.save(curso);
        return convertirAResponse(curso_creado);
    }

    public CursoResponse modificarCurso(Long id, CursoRequest cursoModificado) {
        Curso curso = cursoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Curso",id));

        if(cursoRepository.existsByEmailAndIdNot(cursoModificado.getEmail(),id)){
            throw new RecursoDuplicadoException(
                "Ya existe un curso con ese email " + cursoModificado.getEmail()
            );
        }
        if(cursoRepository.existsByDniAndIdNot(cursoModificado.getDni(),id)){
            throw new RecursoDuplicadoException(
                "Ya existe un curso con ese DNI " + cursoModificado.getDni()
            );
        }

        if(cursoRepository.existsByLegajoAndIdNot(cursoModificado.getLegajo(),id)){
            throw new RecursoDuplicadoException(
                "Ya existe un curso con ese Legajo " + cursoModificado.getLegajo()
            );
        }

        if (!curso.getActivo()) {
            throw new CursoInactivoException(id);
        }

        curso.setNombre(cursoModificado.getNombre());
        curso.setApellido(cursoModificado.getApellido());
        curso.setDni(cursoModificado.getDni());
        curso.setEmail(cursoModificado.getEmail());
        curso.setLegajo(cursoModificado.getLegajo());
        curso.setRol(cursoModificado.getRol());

        Curso cursoGuardado = cursoRepository.save(curso);

        return convertirAResponse(cursoGuardado);
    }

    public void darDeBaja(Long id) {
        Curso curso = cursoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Curso",id));


        if (!curso.getActivo()) {
            throw new RecursoInactivoException("Curso",id);
        }
        curso.setActivo(false);
        cursoRepository.save(curso);
    }
}