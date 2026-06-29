// Tipos que reflejan los DTOs del backend QuickSplit.

export interface User {
  id: number;
  name: string;
  email: string;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  user: User;
}

export interface GroupSummary {
  id: number;
  name: string;
  description: string | null;
  memberCount: number;
  createdAt: string;
}

export interface Group {
  id: number;
  name: string;
  description: string | null;
  ownerId: number;
  createdAt: string;
  members: User[];
}

export interface Share {
  user: User;
  amount: number;
}

export interface Expense {
  id: number;
  description: string;
  amount: number;
  paidBy: User;
  createdAt: string;
  shares: Share[];
}

export interface Balance {
  user: User;
  balance: number;
}

export interface SettlementTransaction {
  from: User;
  to: User;
  amount: number;
}

export interface GroupSettlement {
  balances: Balance[];
  transactions: SettlementTransaction[];
}

export type SplitType = 'EQUAL' | 'EXACT';

export interface CreateExpenseRequest {
  description: string;
  amount: number;
  paidByUserId: number;
  splitType: SplitType;
  participantUserIds?: number[];
  shares?: { userId: number; amount: number }[];
}
