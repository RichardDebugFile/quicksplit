import { useEffect, useState, type FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { api, errorMessage } from '../api/client';
import type { GroupSummary } from '../api/types';
import { AppHeader } from '../components/AppHeader';
import { formatDate } from '../lib/format';

export function DashboardPage() {
  const [groups, setGroups] = useState<GroupSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [creating, setCreating] = useState(false);

  async function loadGroups() {
    try {
      const res = await api.get<GroupSummary[]>('/api/groups');
      setGroups(res.data);
    } catch (err) {
      setError(errorMessage(err, 'No se pudieron cargar los grupos'));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadGroups();
  }, []);

  async function onCreate(event: FormEvent) {
    event.preventDefault();
    setError(null);
    setCreating(true);
    try {
      await api.post('/api/groups', { name, description });
      setName('');
      setDescription('');
      await loadGroups();
    } catch (err) {
      setError(errorMessage(err, 'No se pudo crear el grupo'));
    } finally {
      setCreating(false);
    }
  }

  return (
    <>
      <AppHeader />
      <main className="container">
        <h2>Tus grupos</h2>

        <form className="card" onSubmit={onCreate} style={{ marginBottom: '1.5rem' }}>
          <strong>Nuevo grupo</strong>
          <label htmlFor="group-name">Nombre</label>
          <input
            id="group-name"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="Viaje a la playa"
            required
          />
          <label htmlFor="group-desc">Descripcion (opcional)</label>
          <input
            id="group-desc"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="Fin de semana con amigos"
          />
          {error && <div className="error" role="alert">{error}</div>}
          <button className="btn-primary" type="submit" disabled={creating}>
            {creating ? 'Creando…' : 'Crear grupo'}
          </button>
        </form>

        {loading ? (
          <p className="muted">Cargando grupos…</p>
        ) : groups.length === 0 ? (
          <p className="muted">Aun no tienes grupos. Crea el primero arriba.</p>
        ) : (
          <div className="grid">
            {groups.map((group) => (
              <Link key={group.id} to={`/groups/${group.id}`} className="group-card">
                <div className="row between">
                  <strong>{group.name}</strong>
                  <span className="pill">{group.memberCount} miembros</span>
                </div>
                {group.description && <p className="muted" style={{ margin: '0.4rem 0 0' }}>{group.description}</p>}
                <p className="muted" style={{ margin: '0.4rem 0 0', fontSize: '0.8rem' }}>
                  Creado el {formatDate(group.createdAt)}
                </p>
              </Link>
            ))}
          </div>
        )}
      </main>
    </>
  );
}
