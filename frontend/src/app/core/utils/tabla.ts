import { signal } from '@angular/core';

export type DireccionOrden = 'asc' | 'desc';

// estado de ordenamiento de una tabla
export class TableSorter {
  readonly campo = signal<string | null>(null);
  readonly direccion = signal<DireccionOrden>('asc');

  toggle(campo: string): void {
    if (this.campo() === campo) {
      this.direccion.set(this.direccion() === 'asc' ? 'desc' : 'asc');
    } else {
      this.campo.set(campo);
      this.direccion.set('asc');
    }
  }

  // flecha a mostrar en el encabezado ('' si no esta ordenado)
  flecha(campo: string): string {
    if (this.campo() !== campo) {
      return '';
    }
    return this.direccion() === 'asc' ? '▲' : '▼';
  }

  ordenar<T>(items: T[]): T[] {
    return ordenarPor(items, this.campo(), this.direccion());
  }
}

// ordena por campo (soporta rutas anidadas como 'materia.nombre')
export function ordenarPor<T>(
  items: T[],
  campo: string | null,
  direccion: DireccionOrden,
): T[] {
  if (!campo) {
    return items;
  }
  const resultado = [...items].sort((a, b) =>
    comparar(valorDe(a, campo), valorDe(b, campo)),
  );
  return direccion === 'desc' ? resultado.reverse() : resultado;
}

// filtra por termino contra todos los valores del objeto
export function filtrar<T>(items: T[], termino: string): T[] {
  const t = termino.trim().toLowerCase();
  if (!t) {
    return items;
  }
  return items.filter((item) => aTexto(item).toLowerCase().includes(t));
}

// valor por ruta con puntos, ej: 'materia.nombre'
function valorDe(item: unknown, ruta: string): unknown {
  return ruta.split('.').reduce<unknown>((acc, clave) => {
    if (acc == null) {
      return undefined;
    }
    return (acc as Record<string, unknown>)[clave];
  }, item);
}

// convierte cualquier valor a texto plano
function aTexto(valor: unknown): string {
  if (valor == null) {
    return '';
  }
  if (typeof valor === 'boolean') {
    return valor ? 'sí' : 'no';
  }
  if (Array.isArray(valor)) {
    return valor.map(aTexto).join(' ');
  }
  if (typeof valor === 'object') {
    return Object.values(valor as object).map(aTexto).join(' ');
  }
  return String(valor);
}

function comparar(a: unknown, b: unknown): number {
  if (a == null && b == null) {
    return 0;
  }
  if (a == null) {
    return 1;
  }
  if (b == null) {
    return -1;
  }

  if (typeof a === 'number' && typeof b === 'number') {
    return a - b;
  }
  if (typeof a === 'boolean' && typeof b === 'boolean') {
    return Number(a) - Number(b);
  }

  const sa = aTexto(a).toLowerCase();
  const sb = aTexto(b).toLowerCase();
  return sa.localeCompare(sb, 'es', { numeric: true });
}

// estado de paginacion de una tabla
export class Paginator {
  readonly pagina = signal(0);
  readonly tamanoPagina = signal(10);

  reset(): void {
    this.pagina.set(0);
  }

  totalPaginas(total: number): number {
    return Math.max(1, Math.ceil(total / this.tamanoPagina()));
  }

  paginaActual(total: number): number {
    return Math.min(this.pagina(), this.totalPaginas(total) - 1);
  }
}
