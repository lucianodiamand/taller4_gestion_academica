import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { SearchBar } from '../../core/components/search-bar/search-bar';
import { Pagination } from '../../core/components/pagination/pagination';
import { Rol } from '../../core/models/auth.model';
import { AuthService } from '../../core/services/auth.service';
import { ConfirmService } from '../../core/services/confirm.service';
import { Curso, CursoMateria, CursoRequest, CursoService } from '../../core/services/curso.service';
import { Usuario, UsuarioService } from '../../core/services/usuario.service';
import { Paginator, TableSorter, filtrar } from '../../core/utils/tabla';

type ModoFormulario = 'crear' | 'editar' | null;

@Component({
  selector: 'app-cursos',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, SearchBar, Pagination],
  templateUrl: './cursos.html',
  styleUrl: './cursos.css',
})
export class Cursos implements OnInit {
  private readonly cursoService = inject(CursoService);
  private readonly usuarioService = inject(UsuarioService);
  private readonly confirmService = inject(ConfirmService);
  protected readonly authService = inject(AuthService);
  private readonly fb = inject(FormBuilder);

  // Permisos: Solo ADMIN puede crear, editar o eliminar cursos
  protected readonly puedeGestionar = this.authService.tienePermiso([Rol.ADMIN]);

  // Señales de estado
  protected readonly cursos = signal<Curso[]>([]);
  protected readonly materias = signal<CursoMateria[]>([]);
  protected readonly profesores = signal<Usuario[]>([]);
  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly termino = signal('');
  protected readonly sorter = new TableSorter();
  protected readonly paginator = new Paginator();
  protected readonly filas = computed(() =>
    this.sorter.ordenar(filtrar(this.cursos(), this.termino())),
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

  private cursoEnEdicion: Curso | null = null;

  protected readonly form = this.fb.nonNullable.group({
    anio: [new Date().getFullYear(), [Validators.required, Validators.min(2000), Validators.max(2100)]],
    cuatrimestre: [1, [Validators.required, Validators.min(1), Validators.max(2)]],
    comision: ['', [Validators.required, Validators.maxLength(50)]],
    materiaId: [null as number | null, [Validators.required]],
    profesorId: [null as number | null, [Validators.required]],
  });

  ngOnInit(): void {
    this.cargarCursos();

    // Si tiene permiso para gestionar cursos, precargamos materias y profesores para el formulario
    if (this.puedeGestionar) {
      this.cargarMaterias();
      this.cargarProfesores();
    }
  }

  cargarCursos(): void {
    this.cargando.set(true);
    this.error.set(null);

    this.cursoService.listar().subscribe({
      next: (cursos) => {
        this.cursos.set(cursos);
        this.cargando.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.cargando.set(false);
        this.error.set(
          err.status === 403
            ? 'No tenés permisos para ver la lista de cursos.'
            : 'No se pudo cargar la lista de cursos. Probá de nuevo en unos momentos.'
        );
      },
    });
  }

  cargarMaterias(): void {
    this.cursoService.listarMaterias().subscribe({
      next: (materias) => {
        // Mostramos solo las activas
        this.materias.set(materias.filter((m) => m.activo));
      },
      error: () => {
        console.error('No se pudieron cargar las materias disponibles.');
      },
    });
  }

  cargarProfesores(): void {
    this.usuarioService.listar().subscribe({
      next: (usuarios) => {
        // Filtramos usuarios con rol PROFESOR que estén activos
        this.profesores.set(usuarios.filter((u) => u.rol === Rol.PROFESOR && u.activo));
      },
      error: () => {
        console.error('No se pudieron cargar los profesores disponibles.');
      },
    });
  }

  abrirFormularioCrear(): void {
    this.cursoEnEdicion = null;
    this.errorFormulario.set(null);

    const materiaInicial = this.materias().length > 0 ? this.materias()[0].id : null;
    const profesorInicial = this.profesores().length > 0 ? this.profesores()[0].id : null;

    this.form.reset({
      anio: new Date().getFullYear(),
      cuatrimestre: 1,
      comision: '',
      materiaId: materiaInicial,
      profesorId: profesorInicial,
    });

    this.modo.set('crear');
  }

  abrirFormularioEditar(curso: Curso): void {
    this.cursoEnEdicion = curso;
    this.errorFormulario.set(null);

    this.form.reset({
      anio: curso.anio,
      cuatrimestre: curso.cuatrimestre,
      comision: curso.comision,
      materiaId: curso.materia.id,
      profesorId: curso.profesor.id,
    });

    this.modo.set('editar');
  }

  cerrarFormulario(): void {
    this.modo.set(null);
    this.cursoEnEdicion = null;
    this.errorFormulario.set(null);
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const valores = this.form.getRawValue();

    if (!valores.materiaId || !valores.profesorId) {
      this.errorFormulario.set('Debes seleccionar una materia y un profesor.');
      return;
    }

    const payload: CursoRequest = {
      anio: Number(valores.anio),
      cuatrimestre: Number(valores.cuatrimestre),
      comision: valores.comision.trim(),
      materiaId: Number(valores.materiaId),
      profesorId: Number(valores.profesorId),
    };

    this.guardando.set(true);
    this.errorFormulario.set(null);

    if (this.modo() === 'editar' && this.cursoEnEdicion) {
      this.cursoService.modificar(this.cursoEnEdicion.id, payload).subscribe({
        next: () => this.onGuardadoExitoso(),
        error: (err: HttpErrorResponse) => this.onErrorGuardando(err),
      });
      return;
    }

    this.cursoService.crear(payload).subscribe({
      next: () => this.onGuardadoExitoso(),
      error: (err: HttpErrorResponse) => this.onErrorGuardando(err),
    });
  }

  eliminar(curso: Curso): void {
    this.confirmService
      .confirmar({
        titulo: 'Dar de baja curso',
        mensaje: `¿Dar de baja el curso de ${curso.materia.nombre} (${curso.anio} - ${curso.cuatrimestre}º Cuat., Comisión "${curso.comision}")?`,
        textoConfirmar: 'Dar de baja',
        textoCancelar: 'Cancelar',
      })
      .subscribe((confirmado) => {
        if (!confirmado) {
          return;
        }

        this.error.set(null);
        this.cursoService.eliminar(curso.id).subscribe({
          next: () => this.cargarCursos(),
          error: (err: HttpErrorResponse) => {
            const mensaje =
              typeof err.error === 'string'
                ? err.error
                : err.error?.message || 'No se pudo dar de baja el curso.';
            this.error.set(mensaje);
          },
        });
      });
  }

  private onGuardadoExitoso(): void {
    this.guardando.set(false);
    this.cerrarFormulario();
    this.cargarCursos();
  }

  private onErrorGuardando(err: HttpErrorResponse): void {
    this.guardando.set(false);
    let mensaje = 'No se pudo guardar el curso. Verificá los datos e intentá de nuevo.';

    if (typeof err.error === 'string' && err.error.length > 0) {
      mensaje = err.error;
    } else if (err.error?.message) {
      mensaje = err.error.message;
    }

    this.errorFormulario.set(mensaje);
  }
}
