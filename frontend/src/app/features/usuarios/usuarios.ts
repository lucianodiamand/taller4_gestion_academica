import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { Rol } from '../../core/models/auth.model';
import { Usuario, UsuarioRequest, UsuarioService } from '../../core/services/usuario.service';

type ModoFormulario = 'crear' | 'editar' | null;

@Component({
  selector: 'app-usuarios',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './usuarios.html',
  styleUrl: './usuarios.css',
})
export class Usuarios implements OnInit {
  private readonly usuarioService = inject(UsuarioService);
  private readonly fb = inject(FormBuilder);

  protected readonly roles = Object.values(Rol);

  protected readonly usuarios = signal<Usuario[]>([]);
  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly modo = signal<ModoFormulario>(null);
  protected readonly guardando = signal(false);
  protected readonly errorFormulario = signal<string | null>(null);

  private usuarioEnEdicion: Usuario | null = null;

  protected readonly form = this.fb.nonNullable.group({
    nombre: ['', Validators.required],
    apellido: ['', Validators.required],
    legajo: ['', Validators.required],
    dni: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    password: [''],
    rol: [Rol.ALUMNO, Validators.required],
  });

  ngOnInit(): void {
    this.cargarUsuarios();
  }

  cargarUsuarios(): void {
    this.cargando.set(true);
    this.error.set(null);

    this.usuarioService.listar().subscribe({
      next: (usuarios) => {
        this.usuarios.set(usuarios);
        this.cargando.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.cargando.set(false);
        this.error.set(
          err.status === 403
            ? 'No tenés permiso para ver la lista de usuarios.'
            : 'No se pudo cargar la lista de usuarios. Probá de nuevo en unos segundos.'
        );
      },
    });
  }

  abrirFormularioCrear(): void {
    this.usuarioEnEdicion = null;
    this.errorFormulario.set(null);
    this.form.reset({ rol: Rol.ALUMNO });
    this.form.controls.password.addValidators(Validators.required);
    this.form.controls.password.updateValueAndValidity();
    this.modo.set('crear');
  }

  abrirFormularioEditar(usuario: Usuario): void {
    this.usuarioEnEdicion = usuario;
    this.errorFormulario.set(null);
    this.form.controls.password.clearValidators();
    this.form.controls.password.updateValueAndValidity();
    this.form.reset({
      nombre: usuario.nombre,
      apellido: usuario.apellido,
      legajo: usuario.legajo,
      dni: '',
      email: usuario.email,
      password: '',
      rol: usuario.rol,
    });
    this.modo.set('editar');
  }

  cerrarFormulario(): void {
    this.modo.set(null);
    this.usuarioEnEdicion = null;
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const valores = this.form.getRawValue();
    const payload: UsuarioRequest = {
      nombre: valores.nombre,
      apellido: valores.apellido,
      legajo: valores.legajo,
      dni: valores.dni,
      email: valores.email,
      rol: valores.rol,
    };

    this.guardando.set(true);
    this.errorFormulario.set(null);

    if (this.modo() === 'editar' && this.usuarioEnEdicion) {
      this.usuarioService.modificar(this.usuarioEnEdicion.id, payload).subscribe({
        next: () => this.onGuardadoExitoso(),
        error: (err: HttpErrorResponse) => this.onErrorGuardando(err),
      });
      return;
    }

    payload.password = valores.password;
    this.usuarioService.crear(payload).subscribe({
      next: () => this.onGuardadoExitoso(),
      error: (err: HttpErrorResponse) => this.onErrorGuardando(err),
    });
  }

  eliminar(usuario: Usuario): void {
    const confirmado = confirm(`¿Dar de baja a ${usuario.nombre} ${usuario.apellido} (legajo ${usuario.legajo})?`);
    if (!confirmado) {
      return;
    }

    this.error.set(null);
    this.usuarioService.eliminar(usuario.id).subscribe({
      next: () => this.cargarUsuarios(),
      error: (err: HttpErrorResponse) => {
        this.error.set(
          typeof err.error === 'string' ? err.error : 'No se pudo dar de baja al usuario.'
        );
      },
    });
  }

  private onGuardadoExitoso(): void {
    this.guardando.set(false);
    this.cerrarFormulario();
    this.cargarUsuarios();
  }

  private onErrorGuardando(err: HttpErrorResponse): void {
    this.guardando.set(false);
    // El backend devuelve el mensaje como texto plano (ej. "Ya existe un usuario con ese email ...")
    this.errorFormulario.set(
      typeof err.error === 'string' && err.error.length > 0
        ? err.error
        : 'No se pudo guardar el usuario. Revisá los datos e intentá de nuevo.'
    );
  }
}