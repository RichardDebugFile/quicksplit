// Utilidades de formato.

/** Formatea un monto como moneda (USD) con dos decimales y punto decimal. */
export function formatMoney(amount: number): string {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(amount);
}

/** Formatea una fecha ISO a un formato corto legible. */
export function formatDate(iso: string): string {
  const date = new Date(iso);
  if (Number.isNaN(date.getTime())) {
    return iso;
  }
  return date.toLocaleDateString('es-EC', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  });
}

/** Texto descriptivo del balance de un usuario. */
export function balanceLabel(balance: number): string {
  if (balance > 0.005) {
    return 'le deben';
  }
  if (balance < -0.005) {
    return 'debe';
  }
  return 'saldado';
}
