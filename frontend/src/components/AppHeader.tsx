import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/useAuth';

export function AppHeader() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  function onLogout() {
    logout();
    navigate('/login');
  }

  return (
    <header className="app-header">
      <div className="brand" style={{ fontSize: '1.2rem' }}>
        Quick<span>Split</span>
      </div>
      <div className="row">
        {user && <span className="muted">{user.name}</span>}
        <button className="btn-secondary" onClick={onLogout}>
          Salir
        </button>
      </div>
    </header>
  );
}
