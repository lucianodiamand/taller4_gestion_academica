import { Injectable, signal } from '@angular/core';
import { Observable } from 'rxjs';

export interface ConfirmOpciones {
  titulo?: string;
  mensaje?: string;
  textoConfirmar?: string;
  textoCancelar?: string;
}

// dialogo de confirmacion global (reemplaza al confirm() del navegador)
@Injectable({ providedIn: 'root' })
export class ConfirmService {
  private readonly abierto = signal(false);
  private readonly titulo = signal('¿Estás seguro?');
  private readonly mensaje = signal('');
  private readonly textoConfirmar = signal('Confirmar');
  private readonly textoCancelar = signal('Cancelar');

  private resolver: ((valor: boolean) => void) | null = null;

  readonly estado = {
    abierto: this.abierto.asReadonly(),
    titulo: this.titulo.asReadonly(),
    mensaje: this.mensaje.asReadonly(),
    textoConfirmar: this.textoConfirmar.asReadonly(),
    textoCancelar: this.textoCancelar.asReadonly(),
  };

  confirmar(opciones: ConfirmOpciones = {}): Observable<boolean> {
    this.titulo.set(opciones.titulo ?? '¿Estás seguro?');
    this.mensaje.set(opciones.mensaje ?? '');
    this.textoConfirmar.set(opciones.textoConfirmar ?? 'Confirmar');
    this.textoCancelar.set(opciones.textoCancelar ?? 'Cancelar');
    this.abierto.set(true);

    return new Observable<boolean>((subscriber) => {
      this.resolver = (valor) => {
        subscriber.next(valor);
        subscriber.complete();
      };
      return () => {
        this.resolver = null;
      };
    });
  }

  aceptar(): void {
    this.cerrar(true);
  }

  cancelar(): void {
    this.cerrar(false);
  }

  private cerrar(valor: boolean): void {
    this.abierto.set(false);
    this.resolver?.(valor);
    this.resolver = null;
  }
}
