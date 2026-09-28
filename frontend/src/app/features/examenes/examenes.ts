import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { Rol } from '../../core/models/auth.model';
import { AuthService } from '../../core/services/auth.service';
import { Curso, CursoService } from '../../core/services/curso.service';
import {
  Examen,
  ExamenRequest,
  ExamenService,
  TipoExamen,
} from '../../core/services/examen.service';

type ModoFormulario = 'crear' | 'editar' | null;

@Component({
  selector: 'app-examenes',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './examenes.html',
  styleUrl: './examenes.css',
})
export class Examenes implements OnInit {
  protected readonly authService = inject(AuthService);
  private readonly examenService = inject(ExamenService);
  private readonly cursoService = inject(CursoService);
  private readonly fb = inject(FormBuilder);

  protected readonly tiposExamen = Object.values(TipoExamen);
  protected readonly Rol = Rol;

  // Estado reactivo con Signals
  protected readonly examenes = signal<Examen[]>([]);
  protected readonly cursos = signal<Curso[]>([]);
  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly modo = signal<ModoFormulario>(null);
  protected readonly guardando = signal(false);
  protected readonly errorFormulario = signal<string | null>(null);

  private examenEnEdicion: Examen | null = null;

  // Formulario reactivo para alta y edición
  protected readonly form = this.fb.nonNullable.group({
    fecha: ['', Validators.required],
    tipo: [TipoExamen.PARCIAL, Validators.required],
    descripcion: [''],
    cursoId: [0, [Validators.required, Validators.min(1)]],
  });

  // Saber si el usuario conectado puede crear/editar/eliminar
  protected get puedeGestionar(): boolean {
    return this.authService.tienePermiso([Rol.ADMIN, Rol.PROFESOR]);
  }

  ngOnInit(): void {
    this.cargarExamenes();
    if (this.puedeGestionar) {
      this.cargarCursos();
    }
  }

  cargarExamenes(): void {
    this.cargando.set(true);
    this.error.set(null);

    this.examenService.listar().subscribe({
      next: (data) => {
        this.examenes.set(data);
        this.cargando.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.cargando.set(false);
        this.error.set(
          err.status === 403
            ? 'No tenés permiso para ver los exámenes.'
            : 'No se pudo cargar la lista de exámenes. Probá de nuevo en unos segundos.'
        );
      },
    });
  }

  cargarCursos(): void {
    this.cursoService.listar().subscribe({
      next: (data) => this.cursos.set(data),
      error: () => {
        // Si falla la carga de cursos, se avisará al intentar abrir el formulario
      },
    });
  }

  abrirFormularioCrear(): void {
    this.examenEnEdicion = null;
    this.errorFormulario.set(null);

    const primerCursoId = this.cursos().length > 0 ? this.cursos()[0].id : 0;
    this.form.reset({
      fecha: '',
      tipo: TipoExamen.PARCIAL,
      descripcion: '',
      cursoId: primerCursoId,
    });

    this.modo.set('crear');
  }

  abrirFormularioEditar(examen: Examen): void {
    this.examenEnEdicion = examen;
    this.errorFormulario.set(null);

    this.form.reset({
      fecha: examen.fecha,
      tipo: examen.tipo,
      descripcion: examen.descripcion || '',
      cursoId: examen.curso.id,
    });

    this.modo.set('editar');
  }

  cerrarFormulario(): void {
    this.modo.set(null);
    this.examenEnEdicion = null;
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const valores = this.form.getRawValue();
    const payload: ExamenRequest = {
      fecha: valores.fecha,
      tipo: valores.tipo,
      descripcion: valores.descripcion.trim() || undefined,
      cursoId: Number(valores.cursoId),
    };

    this.guardando.set(true);
    this.errorFormulario.set(null);

    if (this.modo() === 'editar' && this.examenEnEdicion) {
      this.examenService.modificar(this.examenEnEdicion.id, payload).subscribe({
        next: () => this.onGuardadoExitoso(),
        error: (err: HttpErrorResponse) => this.onErrorGuardando(err),
      });
      return;
    }

    this.examenService.crear(payload).subscribe({
      next: () => this.onGuardadoExitoso(),
      error: (err: HttpErrorResponse) => this.onErrorGuardando(err),
    });
  }

  eliminar(examen: Examen): void {
    const confirmado = confirm(
      `¿Dar de baja el examen ${examen.tipo} del día ${examen.fecha}?`
    );
    if (!confirmado) {
      return;
    }

    this.error.set(null);
    this.examenService.eliminar(examen.id).subscribe({
      next: () => this.cargarExamenes(),
      error: (err: HttpErrorResponse) => {
        this.error.set(
          typeof err.error === 'string'
            ? err.error
            : 'No se pudo dar de baja el examen.'
        );
      },
    });
  }

  private onGuardadoExitoso(): void {
    this.guardando.set(false);
    this.cerrarFormulario();
    this.cargarExamenes();
  }

  private onErrorGuardando(err: HttpErrorResponse): void {
    this.guardando.set(false);
    this.errorFormulario.set(
      typeof err.error === 'string' && err.error.length > 0
        ? err.error
        : 'No se pudo guardar el examen. Revisá los datos ingresados.'
    );
  }
}
