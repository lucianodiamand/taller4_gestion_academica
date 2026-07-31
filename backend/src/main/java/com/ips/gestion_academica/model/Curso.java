package com.ips.gestion_academica.model;

import jakarta.persistence.*;


@Entity
@Table(name = "cursos")
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Boolean activo = true;

    @Column(nullable = false)
    private Integer anio;

    @Column(nullable = false)
    private Integer cuatrimestre;

    @Column(nullable = false)
    private String comision;


    //TODO: agrear relacion de materia cuando este
    //@ManyToOne
    //@JoinColumn(name = "materia_id", nullable = false)
    //private Materia materia;

    @ManyToOne
    @JoinColumn(name = "profesor_id", nullable = false)
    private Usuario profesor;


    public Curso() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getCuatrimestre() {
        return cuatrimestre;
    }

    public void setCuatrimestre(Integer cuatrimestre) {
        this.cuatrimestre = cuatrimestre;
    }

    public String getComision() {
        return comision;
    }

    public void setComision(String comision) {
        this.comision = comision;
    }

    public Integer getAnio() {
        return anio;
    }

    public void setAnio(Integer anio) {
        this.anio = anio;
    }

    public Usuario getProfesor() {
        return profesor;
    }

    public void setProfesor(Usuario profesor) {
        this.profesor = profesor;
    }

    // public Materia getMateria() {
    //     return profesor;
    // }

    // public void setMateria(Materia materia) {
    //     this.materia = materia;
    // }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}