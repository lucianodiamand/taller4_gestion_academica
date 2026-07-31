package com.ips.gestion_academica.service;

import org.springframework.stereotype.Service;

import com.ips.gestion_academica.dto.materia.MateriaRequest;
import com.ips.gestion_academica.dto.materia.MateriaResponse;
import com.ips.gestion_academica.exception.RecursoDuplicadoException;
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
}
