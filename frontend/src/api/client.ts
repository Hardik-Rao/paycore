import type {
  Account,
  AuditLog,
  FraudAlert,
  PageResponse,
  Payment,
  PaymentStats,
} from '../types'

async function request<T>(url: string, options?: RequestInit): Promise<T> {
  const res = await fetch(url, {
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    ...options,
  })
  if (!res.ok) {
    const err = await res.json().catch(() => ({ message: res.statusText }))
    throw new Error(err.message || `Request failed: ${res.status}`)
  }
  if (res.status === 204) return undefined as T
  return res.json()
}

export const api = {
  getStats: (from: string, to: string) =>
    request<PaymentStats>(`/api/v1/payments/stats?from=${from}&to=${to}`),

  getPayments: (page = 0, size = 20) =>
    request<PageResponse<Payment>>(`/api/v1/payments?page=${page}&size=${size}`),

  getPayment: (id: string) => request<Payment>(`/api/v1/payments/${id}`),

  getAuditTrail: (id: string) =>
    request<AuditLog[]>(`/api/v1/payments/${id}/audit-trail`),

  createPayment: (body: {
    idempotencyKey: string
    payerVpa: string
    payeeVpa: string
    amount: number
    currency: string
  }) =>
    request<Payment>('/api/v1/payments', {
      method: 'POST',
      body: JSON.stringify(body),
    }),

  getAccounts: () => request<Account[]>('/api/v1/accounts'),

  createAccount: (body: {
    vpa: string
    accountHolder: string
    accountType: string
  }) =>
    request<Account>('/api/v1/accounts', {
      method: 'POST',
      body: JSON.stringify(body),
    }),

  getFraudAlerts: (page = 0, size = 20) =>
    request<PageResponse<FraudAlert>>(
      `/api/v1/fraud/alerts?status=PENDING&page=${page}&size=${size}`
    ),

  reviewFraudAlert: (id: string, resolution: string) =>
    request<FraudAlert>(`/api/v1/fraud/alerts/${id}/review`, {
      method: 'POST',
      body: JSON.stringify({ resolution, reviewedBy: 'admin' }),
    }),

  getBalance: (vpa: string) =>
    request<{ vpa: string; balance: number }>(
      `/api/v1/ledger/accounts/${encodeURIComponent(vpa)}/balance`
    ).catch(() => null),
}
