import { describe, expect, it } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { LoginPage } from './LoginPage';
import { AuthProvider } from '../auth/AuthProvider';

function renderLogin() {
  return render(
    <MemoryRouter>
      <AuthProvider>
        <LoginPage />
      </AuthProvider>
    </MemoryRouter>,
  );
}

describe('LoginPage', () => {
  it('muestra el formulario de inicio de sesion', () => {
    renderLogin();
    expect(screen.getByRole('button', { name: /iniciar sesion/i })).toBeInTheDocument();
    expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/contrasena/i)).toBeInTheDocument();
  });

  it('ofrece un enlace para registrarse', () => {
    renderLogin();
    expect(screen.getByRole('link', { name: /registrate/i })).toBeInTheDocument();
  });
});
