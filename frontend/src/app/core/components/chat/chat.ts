import { HttpErrorResponse } from '@angular/common/http';
import { Component, effect, inject, signal } from '@angular/core';

import { AuthService } from '../../services/auth.service';
import { ChatHistoryMessage, ChatService } from '../../services/chat.service';
import { MarkdownPipe } from './markdown.pipe';

interface Mensaje {
  rol: 'usuario' | 'bot';
  texto: string;
}

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [MarkdownPipe],
  templateUrl: './chat.html',
  styleUrl: './chat.css',
})
export class Chat {
  private readonly chatService = inject(ChatService);
  protected readonly authService = inject(AuthService);

  protected readonly abierto = signal(false);
  protected readonly mensajes = signal<Mensaje[]>([]);
  protected readonly escribiendo = signal(false);
  protected readonly nuevoMensaje = signal('');

  constructor() {
    effect(() => {
      if (!this.authService.estaLogueado()) {
        this.mensajes.set([]);
        this.nuevoMensaje.set('');
        this.abierto.set(false);
        this.escribiendo.set(false);
      }
    });
  }

  toggle(): void {
    this.abierto.update((v) => !v);
  }

  enviar(): void {
    const texto = this.nuevoMensaje().trim();
    if (!texto || this.escribiendo()) {
      return;
    }

    const historial: ChatHistoryMessage[] = this.mensajes().map(
      (m): ChatHistoryMessage => ({
        role: m.rol === 'usuario' ? 'user' : 'assistant',
        content: m.texto,
      }),
    );

    this.mensajes.update((m) => [...m, { rol: 'usuario', texto }]);
    this.nuevoMensaje.set('');
    this.escribiendo.set(true);

    this.chatService.enviar(texto, historial).subscribe({
      next: (resp) => {
        this.mensajes.update((m) => [...m, { rol: 'bot', texto: resp.reply }]);
        this.escribiendo.set(false);
      },
      error: (_err: HttpErrorResponse) => {
        this.mensajes.update((m) => [
          ...m,
          { rol: 'bot', texto: 'No pude responder. Intentá de nuevo.' },
        ]);
        this.escribiendo.set(false);
      },
    });
  }
}
