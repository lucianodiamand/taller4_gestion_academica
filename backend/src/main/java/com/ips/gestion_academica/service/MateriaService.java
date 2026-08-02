package com.ips.gestion_academica.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ips.gestion_academica.dto.materia.MateriaRequest;
import com.ips.gestion_academica.dto.materia.MateriaResponse;
import com.ips.gestion_academica.exception.RecursoDuplicadoException;
import com.ips.gestion_academica.exception.RecursoInactivoException;
import com.ips.gestion_academica.exception.RecursoNoEncontradoException;
import com.ips.gestion_academica.model.Materia;
import com.ips.gestion_academica.repository.MateriaRepository;

@Service
public class MateriaService {
    private final MateriaRepository materiaRepository;

    public MateriaService(MateriaRepository materiaRepository) {
        this.materiaRepository = materiaRepository;
    }

    private MateriaResponse convertirAResponse(Materia materia) {
        return new MateriaResponse(
            materia.getId(),
            materia.getCodigo(),
            materia.getNombre(),
            materia.getDescripcion(),
            materia.getAnioCursada(),
            materia.getActivo()
        );
    }

    public MateriaResponse crearMateria(MateriaRequest request) {
        if (materiaRepository.existsByCodigo(request.getCodigo())) {
            throw new RecursoDuplicadoException("Ya existe una materia con el código: " + request.getCodigo());
        }

        Materia materia = new Materia();
        materia.setCodigo(request.getCodigo());
        materia.setNombre(request.getNombre());
        materia.setDescripcion(request.getDescripcion());
        materia.setAnioCursada(request.getAnioCursada());
        materia.setActivo(true);

        return convertirAResponse(materiaRepository.save(materia));
    }

    public MateriaResponse obtenerMateriaPorId(Long id) {
        Materia materia = materiaRepository.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Materia", id));
        return convertirAResponse(materia);
    }

    public List<MateriaResponse> obtenerMateriasActivas() {
        List<Materia> materiasActivas = materiaRepository.findByActivoTrue();
        return materiasActivas.stream()
                .map(this::convertirAResponse)
                .toList();
    }

    public void eliminarMateria(Long id) {
        Materia materia = materiaRepository.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Materia", id));

        if (!materia.getActivo()) {
            throw new RecursoInactivoException("Materia", id);
        }
        
        materia.setActivo(false);
        materiaRepository.save(materia);
    }

    public MateriaResponse actualizarMateria(Long id, MateriaRequest request) {
        Materia materia = materiaRepository.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Materia", id));

        if (!materia.getCodigo().equals(request.getCodigo()) && materiaRepository.existsByCodigo(request.getCodigo())) {
            throw new RecursoDuplicadoException("Ya existe una materia con el código: " + request.getCodigo());
        }

        materia.setCodigo(request.getCodigo());
        materia.setNombre(request.getNombre());
        materia.setDescripcion(request.getDescripcion());
        materia.setAnioCursada(request.getAnioCursada());

        return convertirAResponse(materiaRepository.save(materia));
    }
}
