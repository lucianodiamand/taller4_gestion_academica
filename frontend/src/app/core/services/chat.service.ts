import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface ChatHistoryMessage {
  role: 'user' | 'assistant';
  content: string;
}

@Injectable({ providedIn: 'root' })
export class ChatService {
  private readonly apiUrl = '/api/chat';

  constructor(private readonly http: HttpClient) {}

  enviar(mensaje: string, historial: ChatHistoryMessage[]): Observable<{ reply: string }> {
    return this.http.post<{ reply: string }>(this.apiUrl, {
      message: mensaje,
      history: historial,
    });
  }
}
