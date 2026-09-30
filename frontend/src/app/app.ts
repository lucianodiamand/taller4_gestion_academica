import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

import { Chat } from './core/components/chat/chat';
import { ConfirmDialog } from './core/components/confirm-dialog/confirm-dialog';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, ConfirmDialog, Chat],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {}
