package com.ips.gestion_academica.service;

import com.ips.gestion_academica.dto.curso.CursoRequest;
import com.ips.gestion_academica.dto.curso.CursoResponse;
import com.ips.gestion_academica.dto.usuario.UsuarioResumeResponse;
import com.ips.gestion_academica.dto.materia.MateriaResponse;
import com.ips.gestion_academica.exception.RecursoDuplicadoException;
import com.ips.gestion_academica.exception.RecursoNoEncontradoException;
import com.ips.gestion_academica.exception.RecursoInactivoException;
import com.ips.gestion_academica.model.Curso;
import com.ips.gestion_academica.model.Rol;
import com.ips.gestion_academica.model.Usuario;
import com.ips.gestion_academica.model.Materia;
import com.ips.gestion_academica.repository.CursoRepository;
import com.ips.gestion_academica.repository.UsuarioRepository;
import com.ips.gestion_academica.repository.MateriaRepository;
import org.springframework.stereotype.Service;

import java.util.List;



@Service
public class CursoService {

    private final UsuarioRepository usuarioRepository;
    private final CursoRepository cursoRepository;
    private final MateriaRepository materiaRepository;
    public CursoService(
        CursoRepository cursoRepository, 
        UsuarioRepository usuarioRepository,
        MateriaRepository materiaRepository){
        this.cursoRepository = cursoRepository;
        this.usuarioRepository = usuarioRepository;
        this.materiaRepository = materiaRepository;
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

    private MateriaResponse convertirAResumeMateria(Materia materia) {
        return new MateriaResponse(
            materia.getId(),
            materia.getCodigo(),
            materia.getNombre(),
            materia.getDescripcion(),
            materia.getAnioCursada(),
            materia.getActivo()
        );
    }
    private CursoResponse convertirAResponse(Curso curso) {
        return new CursoResponse(
                curso.getId(),
                curso.getAnio(),
                curso.getCuatrimestre(),
                curso.getComision(),
                curso.getActivo(),
                convertirAResume(curso.getProfesor()),
                convertirAResumeMateria(curso.getMateria())
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
        Usuario profesor = usuarioRepository.findById(request.getProfesorId())
        .orElseThrow(() ->
                new RecursoNoEncontradoException("Usuario Profesor", request.getProfesorId())
        );

        Materia materia = materiaRepository.findById(request.getMateriaId())
        .orElseThrow(() ->
                new RecursoNoEncontradoException("Materia", request.getProfesorId())
        );

        if (!Boolean.TRUE.equals(materia.getActivo())) {
            throw new RecursoInactivoException(
                    "Materia",
                    materia.getId()
            );
        }

        if (!profesor.getActivo()) {
            throw new RecursoInactivoException(
                    "El profesor con ID ",profesor.getId());
        }

        if (profesor.getRol() != Rol.PROFESOR) {
           throw new IllegalArgumentException(
                "El usuario seleccionado no tiene rol de profesor"
            );
        }

        Curso curso = new Curso();
        curso.setAnio(request.getAnio());
        curso.setCuatrimestre(request.getCuatrimestre());
        curso.setComision(request.getComision());
        curso.setProfesor(profesor);
        curso.setMateria(materia);

        curso.setActivo(true);

        if(cursoRepository.existsByAnioAndCuatrimestreAndComisionAndMateria_Id(
            request.getAnio(),
            request.getCuatrimestre(),
            request.getComision(),
            materia.getId()
        )){
            throw new RecursoDuplicadoException(
                "Ya existe un Cusro con ese anio " + curso.getAnio() + " cuatrimestre " + curso.getCuatrimestre() + " comision " + curso.getComision() + "o Materia " + materia.getId()
            );
        }

        Curso curso_creado = cursoRepository.save(curso);
        return convertirAResponse(curso_creado);
    }

    public CursoResponse modificarCurso(Long id, CursoRequest cursoModificado) {
        Curso curso = cursoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Curso",id));

        Materia materia = materiaRepository.findById(cursoModificado.getMateriaId())
        .orElseThrow(() ->
                new RecursoNoEncontradoException("Materia", cursoModificado.getMateriaId())
        );
        if(cursoRepository.existsByAnioAndCuatrimestreAndComisionAndMateria_Id(
            cursoModificado.getAnio(),
            cursoModificado.getCuatrimestre(),
            cursoModificado.getComision(),
            materia.getId()
        )){
            throw new RecursoDuplicadoException(
                "Ya existe un Curso con ese anio " + curso.getAnio() + " cuatrimestre " + curso.getCuatrimestre() + " comision " + curso.getComision() + "o  Materia" + curso.getMateria()
            );
        }
        if (!curso.getActivo()) {
            throw new RecursoInactivoException("Curso",id);
        }

        Usuario profesor = usuarioRepository.findById(cursoModificado.getProfesorId())
        .orElseThrow(() ->
                new RecursoNoEncontradoException("Usuario Profesor", cursoModificado.getProfesorId())
        );

        if (!profesor.getActivo()) {
            throw new RecursoInactivoException(
                    "El profesor con ID ",profesor.getId());
        }

        if (profesor.getRol() != Rol.PROFESOR) {
           throw new IllegalArgumentException(
                "El usuario seleccionado no tiene rol de profesor"
            );
        }
        curso.setAnio(cursoModificado.getAnio());
        curso.setCuatrimestre(cursoModificado.getCuatrimestre());
        curso.setComision(cursoModificado.getComision());
        curso.setProfesor(profesor);

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