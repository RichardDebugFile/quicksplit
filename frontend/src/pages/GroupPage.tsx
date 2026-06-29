import { useCallback, useEffect, useState, type FormEvent } from 'react';
import { useParams } from 'react-router-dom';
import { api, errorMessage } from '../api/client';
import type { Expense, Group, GroupSettlement } from '../api/types';
import { AppHeader } from '../components/AppHeader';
import { balanceLabel, formatMoney } from '../lib/format';

export function GroupPage() {
  const { groupId } = useParams<{ groupId: string }>();
  const [group, setGroup] = useState<Group | null>(null);
  const [expenses, setExpenses] = useState<Expense[]>([]);
  const [settlement, setSettlement] = useState<GroupSettlement | null>(null);
  const [error, setError] = useState<string | null>(null);

  // Formulario de nuevo gasto.
  const [description, setDescription] = useState('');
  const [amount, setAmount] = useState('');
  const [paidBy, setPaidBy] = useState<number | ''>('');

  // Formulario de nuevo miembro.
  const [memberEmail, setMemberEmail] = useState('');

  const load = useCallback(async () => {
    if (!groupId) return;
    try {
      const [g, e, s] = await Promise.all([
        api.get<Group>(`/api/groups/${groupId}`),
        api.get<Expense[]>(`/api/groups/${groupId}/expenses`),
        api.get<GroupSettlement>(`/api/groups/${groupId}/settlement`),
      ]);
      setGroup(g.data);
      setExpenses(e.data);
      setSettlement(s.data);
      if (paidBy === '' && g.data.members.length > 0) {
        setPaidBy(g.data.members[0].id);
      }
    } catch (err) {
      setError(errorMessage(err, 'No se pudo cargar el grupo'));
    }
  }, [groupId, paidBy]);

  useEffect(() => {
    load();
    // Se carga una vez al montar; las acciones llaman load() manualmente.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [groupId]);

  async function onAddMember(event: FormEvent) {
    event.preventDefault();
    setError(null);
    try {
      await api.post(`/api/groups/${groupId}/members`, { email: memberEmail });
      setMemberEmail('');
      await load();
    } catch (err) {
      setError(errorMessage(err, 'No se pudo agregar el miembro'));
    }
  }

  async function onAddExpense(event: FormEvent) {
    event.preventDefault();
    setError(null);
    const parsed = Number(amount);
    if (!Number.isFinite(parsed) || parsed <= 0) {
      setError('El monto debe ser mayor que cero');
      return;
    }
    try {
      await api.post(`/api/groups/${groupId}/expenses`, {
        description,
        amount: parsed,
        paidByUserId: paidBy,
        splitType: 'EQUAL',
      });
      setDescription('');
      setAmount('');
      await load();
    } catch (err) {
      setError(errorMessage(err, 'No se pudo registrar el gasto'));
    }
  }

  if (!group) {
    return (
      <>
        <AppHeader />
        <main className="container">
          {error ? <div className="error">{error}</div> : <p className="muted">Cargando…</p>}
        </main>
      </>
    );
  }

  return (
    <>
      <AppHeader />
      <main className="container">
        <div className="row between">
          <div>
            <h2 style={{ marginBottom: 0 }}>{group.name}</h2>
            {group.description && <p className="muted" style={{ marginTop: '0.3rem' }}>{group.description}</p>}
          </div>
          <span className="pill">{group.members.length} miembros</span>
        </div>

        {error && <div className="error" role="alert">{error}</div>}

        <h3 className="section-title">Miembros</h3>
        <div className="grid">
          {group.members.map((m) => (
            <div key={m.id} className="list-item">
              <span>{m.name}</span>
              <span className="muted">{m.email}</span>
            </div>
          ))}
        </div>
        <form className="inline-form" onSubmit={onAddMember} style={{ marginTop: '0.75rem' }}>
          <input
            type="email"
            value={memberEmail}
            onChange={(e) => setMemberEmail(e.target.value)}
            placeholder="email@de-un-usuario.com"
            aria-label="Email del nuevo miembro"
            required
          />
          <button className="btn-secondary" type="submit">Agregar</button>
        </form>

        <h3 className="section-title">Registrar gasto</h3>
        <form className="card" onSubmit={onAddExpense}>
          <label htmlFor="exp-desc">Descripcion</label>
          <input
            id="exp-desc"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="Cena, super, taxi…"
            required
          />
          <div className="row" style={{ gap: '0.75rem', marginTop: '0.5rem' }}>
            <div style={{ flex: 1 }}>
              <label htmlFor="exp-amount">Monto</label>
              <input
                id="exp-amount"
                type="number"
                step="0.01"
                min="0.01"
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                placeholder="0.00"
                required
              />
            </div>
            <div style={{ flex: 1 }}>
              <label htmlFor="exp-payer">Pagado por</label>
              <select
                id="exp-payer"
                value={paidBy}
                onChange={(e) => setPaidBy(Number(e.target.value))}
              >
                {group.members.map((m) => (
                  <option key={m.id} value={m.id}>{m.name}</option>
                ))}
              </select>
            </div>
          </div>
          <p className="muted" style={{ fontSize: '0.8rem', marginTop: '0.5rem' }}>
            Se divide en partes iguales entre los {group.members.length} miembros.
          </p>
          <button className="btn-primary" type="submit">Agregar gasto</button>
        </form>

        <h3 className="section-title">Gastos</h3>
        {expenses.length === 0 ? (
          <p className="muted">Todavia no hay gastos.</p>
        ) : (
          <div className="grid">
            {expenses.map((exp) => (
              <div key={exp.id} className="list-item">
                <span>
                  <strong>{exp.description}</strong>
                  <br />
                  <span className="muted" style={{ fontSize: '0.85rem' }}>
                    Pagado por {exp.paidBy.name}
                  </span>
                </span>
                <span>{formatMoney(exp.amount)}</span>
              </div>
            ))}
          </div>
        )}

        <h3 className="section-title">Balances</h3>
        <div className="grid">
          {settlement?.balances.map((b) => (
            <div key={b.user.id} className="list-item">
              <span>{b.user.name}</span>
              <span className={b.balance >= 0 ? 'amount-positive' : 'amount-negative'}>
                {balanceLabel(b.balance)} {formatMoney(Math.abs(b.balance))}
              </span>
            </div>
          ))}
        </div>

        <h3 className="section-title">Plan de pagos sugerido</h3>
        {settlement && settlement.transactions.length === 0 ? (
          <p className="muted">Todo saldado. No hacen falta pagos. 🎉</p>
        ) : (
          <div className="grid">
            {settlement?.transactions.map((t, i) => (
              <div key={i} className="settle-line">
                <strong>{t.from.name}</strong> le paga{' '}
                <strong className="amount-positive">{formatMoney(t.amount)}</strong> a{' '}
                <strong>{t.to.name}</strong>
              </div>
            ))}
          </div>
        )}
      </main>
    </>
  );
}
