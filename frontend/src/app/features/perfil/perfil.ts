import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, OnInit, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { AuthService } from '../../core/services/auth.service';
import { Usuario, UsuarioService } from '../../core/services/usuario.service';

function passwordsCoincidenValidator(control: AbstractControl): ValidationErrors | null {
  const nueva = control.get('passwordNueva')?.value;
  const repetir = control.get('passwordRepetir')?.value;
  return nueva && repetir && nueva !== repetir ? { passwordsNoCoinciden: true } : null;
}

@Component({
  selector: 'app-perfil',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './perfil.html',
  styleUrl: './perfil.css',
})
export class Perfil implements OnInit {
  protected readonly authService = inject(AuthService);
  private readonly usuarioService = inject(UsuarioService);
  private readonly fb = inject(FormBuilder);

  // --- Carga inicial del perfil ---
  protected readonly cargando = signal(true);
  protected readonly errorCarga = signal<string | null>(null);

  // --- Formulario de datos personales ---
  protected readonly guardandoDatos = signal(false);
  protected readonly errorDatos = signal<string | null>(null);
  protected readonly exitoDatos = signal(false);

  protected readonly datosForm = this.fb.nonNullable.group({
    nombre: ['', Validators.required],
    apellido: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
  });

  // --- Formulario de contraseña ---
  protected readonly guardandoPassword = signal(false);
  protected readonly errorPassword = signal<string | null>(null);
  protected readonly exitoPassword = signal(false);

  protected readonly passwordForm = this.fb.nonNullable.group(
    {
      passwordActual: ['', Validators.required],
      passwordNueva: ['', [Validators.required, Validators.minLength(6)]],
      passwordRepetir: ['', Validators.required],
    },
    { validators: passwordsCoincidenValidator }
  );

  ngOnInit(): void {
    this.usuarioService.obtenerMiPerfil().subscribe({
      next: (usuario: Usuario) => {
        this.datosForm.setValue({
          nombre: usuario.nombre,
          apellido: usuario.apellido,
          email: usuario.email,
        });
        this.cargando.set(false);
      },
      error: () => {
        this.cargando.set(false);
        this.errorCarga.set('No se pudieron cargar tus datos. Probá recargar la página.');
      },
    });
  }

  guardarDatos(): void {
    if (this.datosForm.invalid) {
      this.datosForm.markAllAsTouched();
      return;
    }

    this.guardandoDatos.set(true);
    this.errorDatos.set(null);
    this.exitoDatos.set(false);

    this.usuarioService.actualizarMiPerfil(this.datosForm.getRawValue()).subscribe({
      next: () => {
        this.guardandoDatos.set(false);
        this.exitoDatos.set(true);
      },
      error: (err: HttpErrorResponse) => {
        this.guardandoDatos.set(false);
        this.errorDatos.set(
          typeof err.error === 'string' && err.error.length > 0
            ? err.error
            : 'No se pudieron guardar los cambios. Intentá de nuevo.'
        );
      },
    });
  }

  guardarPassword(): void {
    if (this.passwordForm.invalid) {
      this.passwordForm.markAllAsTouched();
      return;
    }

    this.guardandoPassword.set(true);
    this.errorPassword.set(null);
    this.exitoPassword.set(false);

    const { passwordActual, passwordNueva } = this.passwordForm.getRawValue();

    this.usuarioService.cambiarPassword({ passwordActual, passwordNueva }).subscribe({
      next: () => {
        this.guardandoPassword.set(false);
        this.exitoPassword.set(true);
        this.passwordForm.reset();
      },
      error: (err: HttpErrorResponse) => {
        this.guardandoPassword.set(false);
        this.errorPassword.set(
          err.status === 401
            ? 'La contraseña actual no es correcta.'
            : typeof err.error === 'string' && err.error.length > 0
              ? err.error
              : 'No se pudo cambiar la contraseña. Intentá de nuevo.'
        );
      },
    });
  }
}