import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { SearchBar } from '../../core/components/search-bar/search-bar';
import { Pagination } from '../../core/components/pagination/pagination';
import { Rol } from '../../core/models/auth.model';
import { AuthService } from '../../core/services/auth.service';
import { ConfirmService } from '../../core/services/confirm.service';
import {
  Materia,
  MateriaRequest,
  MateriaService,
} from '../../core/services/materia.service';
import { Paginator, TableSorter, filtrar } from '../../core/utils/tabla';

type ModoFormulario = 'crear' | 'editar' | null;

@Component({
  selector: 'app-materias',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, SearchBar, Pagination],
  templateUrl: './materias.html',
  styleUrl: './materias.css',
})
export class Materias implements OnInit {
  private readonly materiaService = inject(MateriaService);
  private readonly confirmService = inject(ConfirmService);
  protected readonly authService = inject(AuthService);
  private readonly fb = inject(FormBuilder);

  // solo admin puede crear, editar o eliminar materias
  protected readonly puedeGestionar = this.authService.tienePermiso([Rol.ADMIN]);

  protected readonly materias = signal<Materia[]>([]);
  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);

  // busqueda y ordenamiento
  protected readonly termino = signal('');
  protected readonly sorter = new TableSorter();
  protected readonly paginator = new Paginator();
  protected readonly filas = computed(() =>
    this.sorter.ordenar(filtrar(this.materias(), this.termino())),
  );
  protected readonly totalPaginas = computed(() =>
    this.paginator.totalPaginas(this.filas().length),
  );
  protected readonly paginaActual = computed(() =>
    this.paginator.paginaActual(this.filas().length),
  );
  protected readonly filasPaginadas = computed(() => {
    const inicio = this.paginaActual() * this.paginator.tamanoPagina();
    return this.filas().slice(inicio, inicio + this.paginator.tamanoPagina());
  });

  protected irPagina(pagina: number): void {
    this.paginator.pagina.set(pagina);
  }

  protected readonly modo = signal<ModoFormulario>(null);
  protected readonly guardando = signal(false);
  protected readonly errorFormulario = signal<string | null>(null);

  // codigo autoincremental: se muestra pero no se edita
  protected readonly proximoCodigo = signal('');
  protected readonly codigoAmostrar = computed(() =>
    this.modo() === 'editar'
      ? (this.materiaEnEdicion?.codigo ?? '')
      : this.proximoCodigo(),
  );

  protected readonly materiaDetalle = signal<Materia | null>(null);

  private materiaEnEdicion: Materia | null = null;

  protected readonly form = this.fb.nonNullable.group({
    nombre: ['', Validators.required],
    descripcion: [''],
    contenido: [''],
    anioCursada: [
      1,
      [Validators.required, Validators.min(1), Validators.max(6)],
    ],
  });

  ngOnInit(): void {
    this.cargarMaterias();
  }

  cargarMaterias(): void {
    this.cargando.set(true);
    this.error.set(null);

    this.materiaService.listar().subscribe({
      next: (materias) => {
        this.materias.set(materias);
        this.cargando.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.cargando.set(false);
        this.error.set(
          err.status === 403
            ? 'No tenés permiso para ver la lista de materias.'
            : 'No se pudo cargar la lista de materias. Probá de nuevo en unos segundos.',
        );
      },
    });
  }

  abrirFormularioCrear(): void {
    this.materiaEnEdicion = null;
    this.errorFormulario.set(null);
    this.form.reset({ nombre: '', descripcion: '', contenido: '', anioCursada: 1 });
    this.modo.set('crear');
    this.cargarProximoCodigo();
  }

  abrirFormularioEditar(materia: Materia): void {
    this.materiaEnEdicion = materia;
    this.errorFormulario.set(null);
    this.form.reset({
      nombre: materia.nombre,
      descripcion: materia.descripcion ?? '',
      contenido: materia.contenido ?? '',
      anioCursada: materia.anioCursada,
    });
    this.modo.set('editar');
  }

  cerrarFormulario(): void {
    this.modo.set(null);
    this.materiaEnEdicion = null;
    this.errorFormulario.set(null);
  }

  abrirDetalle(materia: Materia): void {
    this.materiaDetalle.set(materia);
  }

  cerrarDetalle(): void {
    this.materiaDetalle.set(null);
  }

  private cargarProximoCodigo(): void {
    this.proximoCodigo.set('');
    this.materiaService.obtenerProximoCodigo().subscribe({
      next: (respuesta) => this.proximoCodigo.set(respuesta.codigo),
      error: () => {
        // no es critico: el codigo lo genera igual el backend al guardar
        this.proximoCodigo.set('');
      },
    });
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const valores = this.form.getRawValue();
    const payload: MateriaRequest = {
      nombre: valores.nombre.trim(),
      descripcion: valores.descripcion.trim() || undefined,
      contenido: valores.contenido.trim() || undefined,
      anioCursada: Number(valores.anioCursada),
    };

    this.guardando.set(true);
    this.errorFormulario.set(null);

    if (this.modo() === 'editar' && this.materiaEnEdicion) {
      this.materiaService
        .modificar(this.materiaEnEdicion.id, payload)
        .subscribe({
          next: () => this.onGuardadoExitoso(),
          error: (err: HttpErrorResponse) => this.onErrorGuardando(err),
        });
      return;
    }

    this.materiaService.crear(payload).subscribe({
      next: () => this.onGuardadoExitoso(),
      error: (err: HttpErrorResponse) => this.onErrorGuardando(err),
    });
  }

  eliminar(materia: Materia): void {
    this.confirmService
      .confirmar({
        titulo: 'Dar de baja materia',
        mensaje: `¿Dar de baja la materia "${materia.nombre}" (${materia.codigo})?`,
        textoConfirmar: 'Dar de baja',
        textoCancelar: 'Cancelar',
      })
      .subscribe((confirmado) => {
        if (!confirmado) {
          return;
        }

        this.error.set(null);
        this.materiaService.eliminar(materia.id).subscribe({
          next: () => this.cargarMaterias(),
          error: (err: HttpErrorResponse) => {
            this.error.set(
              typeof err.error === 'string'
                ? err.error
                : 'No se pudo dar de baja la materia.',
            );
          },
        });
      });
  }

  private onGuardadoExitoso(): void {
    this.guardando.set(false);
    this.cerrarFormulario();
    this.cargarMaterias();
  }

  private onErrorGuardando(err: HttpErrorResponse): void {
    this.guardando.set(false);
    this.errorFormulario.set(
      typeof err.error === 'string' && err.error.length > 0
        ? err.error
        : 'No se pudo guardar la materia. Revisá los datos e intentá de nuevo.',
    );
  }
}
