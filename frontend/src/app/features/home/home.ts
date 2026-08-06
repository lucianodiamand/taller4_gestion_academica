import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

import { AuthService } from '../../core/services/auth.service';
import { Rol } from '../../core/models/auth.model';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home {

  protected readonly Rol = Rol;

  constructor(protected readonly authService: AuthService) {}

}