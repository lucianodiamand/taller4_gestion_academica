export enum Rol {
  ADMIN = 'ADMIN',
  PROFESOR = 'PROFESOR',
  ALUMNO = 'ALUMNO',
}

export interface LoginRequest {
  legajo: string;
  password: string;
}

export interface LoginResponse {
  token: string;
}

/** Datos que se pueden extraer del payload del JWT sin llamar al backend. */
export interface UsuarioAutenticado {
  legajo: string;
  rol: Rol;
}
