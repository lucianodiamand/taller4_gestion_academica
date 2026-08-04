package com.ips.gestion_academica.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(
    name = "examenes",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_examen_curso_fecha_tipo",
            columnNames = {"curso_id", "fecha", "tipo"}
        )
    }
)
public class Examen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoExamen tipo;

    private String descripcion;

    @ManyToOne
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

    @Column(nullable = false)
    private Boolean activo = true;

    public Examen() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public TipoExamen getTipo() {
        return tipo;
    }

    public void setTipo(TipoExamen tipo) {
        this.tipo = tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Curso getCurso() {
        return curso;
    }

    public void setCurso(Curso curso) {
        this.curso = curso;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}