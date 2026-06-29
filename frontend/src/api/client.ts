import axios from 'axios';

const TOKEN_KEY = 'quicksplit.token';

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token);
}

export function clearToken(): void {
  localStorage.removeItem(TOKEN_KEY);
}

// Cliente HTTP central. Usa rutas relativas: en dev el proxy de Vite y en prod
// nginx redirigen /api hacia el backend.
export const api = axios.create({
  baseURL: '/',
  headers: { 'Content-Type': 'application/json' },
});

// Adjunta el token JWT a cada peticion si existe.
api.interceptors.request.use((config) => {
  const token = getToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Si el backend responde 401, se limpia la sesion.
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      clearToken();
    }
    return Promise.reject(error);
  },
);

// Extrae un mensaje de error legible de una respuesta del backend.
export function errorMessage(error: unknown, fallback = 'Ocurrio un error'): string {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data as { message?: string } | undefined;
    return data?.message ?? error.message ?? fallback;
  }
  return fallback;
}
