import { describe, expect, it } from 'vitest';
import { balanceLabel, formatDate, formatMoney } from './format';

describe('formatMoney', () => {
  it('formatea con dos decimales y simbolo de moneda', () => {
    expect(formatMoney(10)).toContain('10.00');
    expect(formatMoney(3.5)).toContain('3.50');
  });

  it('formatea cero', () => {
    expect(formatMoney(0)).toContain('0.00');
  });
});

describe('balanceLabel', () => {
  it('detecta acreedor, deudor y saldado', () => {
    expect(balanceLabel(15)).toBe('le deben');
    expect(balanceLabel(-15)).toBe('debe');
    expect(balanceLabel(0)).toBe('saldado');
    expect(balanceLabel(0.001)).toBe('saldado');
  });
});

describe('formatDate', () => {
  it('formatea una fecha ISO valida', () => {
    expect(formatDate('2026-06-28T12:00:00Z')).toMatch(/2026/);
  });

  it('devuelve el valor original si la fecha es invalida', () => {
    expect(formatDate('no-es-fecha')).toBe('no-es-fecha');
  });
});
