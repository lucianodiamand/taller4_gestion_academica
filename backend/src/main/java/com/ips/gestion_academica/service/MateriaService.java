package com.ips.gestion_academica.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ips.gestion_academica.dto.materia.MateriaRequest;
import com.ips.gestion_academica.dto.materia.MateriaResponse;
import com.ips.gestion_academica.exception.RecursoInactivoException;
import com.ips.gestion_academica.exception.RecursoNoEncontradoException;
import com.ips.gestion_academica.model.Materia;
import com.ips.gestion_academica.repository.MateriaRepository;

@Service
public class MateriaService {

    private static final String PREFIJO_CODIGO = "MAT";

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
            materia.getContenido(),
            materia.getAnioCursada(),
            materia.getActivo()
        );
    }

    // calcula el proximo codigo autoincremental (max existente + 1, evita colisiones con inactivas)
    public String obtenerProximoCodigo() {
        int maximo = 0;

        for (Materia materia : materiaRepository.findAll()) {
            String codigo = materia.getCodigo();
            if (codigo == null || !codigo.startsWith(PREFIJO_CODIGO + "-")) {
                continue;
            }
            try {
                int numero = Integer.parseInt(codigo.substring((PREFIJO_CODIGO + "-").length()));
                if (numero > maximo) {
                    maximo = numero;
                }
            } catch (NumberFormatException e) {
                // se ignoran los codigos con formato distinto
            }
        }

        return PREFIJO_CODIGO + "-" + String.format("%03d", maximo + 1);
    }

    public MateriaResponse crearMateria(MateriaRequest request) {
        Materia materia = new Materia();
        materia.setCodigo(obtenerProximoCodigo());
        materia.setNombre(request.getNombre());
        materia.setDescripcion(request.getDescripcion());
        materia.setContenido(request.getContenido());
        materia.setAnioCursada(request.getAnioCursada());
        materia.setActivo(true);

        return convertirAResponse(materiaRepository.save(materia));
    }

    public MateriaResponse obtenerMateriaPorId(Long id) {
        Materia materia = materiaRepository.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Materia", id));

        if (!Boolean.TRUE.equals(materia.getActivo())) {
            throw new RecursoInactivoException("Materia", id);
        }

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

        if (!Boolean.TRUE.equals(materia.getActivo())) {
            throw new RecursoInactivoException("Materia", id);
        }

        materia.setActivo(false);
        materiaRepository.save(materia);
    }

    public MateriaResponse actualizarMateria(Long id, MateriaRequest request) {
        Materia materia = materiaRepository.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Materia", id));

        if (!Boolean.TRUE.equals(materia.getActivo())) {
            throw new RecursoInactivoException("Materia", id);
        }

        // el codigo no se modifica en la edicion
        materia.setNombre(request.getNombre());
        materia.setDescripcion(request.getDescripcion());
        materia.setContenido(request.getContenido());
        materia.setAnioCursada(request.getAnioCursada());

        return convertirAResponse(materiaRepository.save(materia));
    }
}
