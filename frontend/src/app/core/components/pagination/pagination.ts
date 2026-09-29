import { Component, input, output } from '@angular/core';

@Component({
  selector: 'app-pagination',
  standalone: true,
  templateUrl: './pagination.html',
  styleUrl: './pagination.css',
})
export class Pagination {
  readonly pagina = input(0);
  readonly totalPaginas = input(1);
  readonly paginaCambiada = output<number>();
}
