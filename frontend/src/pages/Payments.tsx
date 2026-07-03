import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api/client'
import type { Payment } from '../types'
import StatusBadge from '../components/StatusBadge'

function formatAmount(n: number) {
  return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' }).format(n)
}

export default function Payments() {
  const [payments, setPayments] = useState<Payment[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.getPayments(0, 50).then((p) => setPayments(p.content)).catch(console.error).finally(() => setLoading(false))
  }, [])

  return (
    <div>
      <div className="mb-8 flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-heading">Payments</h1>
          <p className="mt-1 text-muted">All transaction history</p>
        </div>
        <Link to="/payments/new" className="btn-primary no-underline">
          + New Payment
        </Link>
      </div>

      <div className="glow-border overflow-hidden rounded-xl bg-slate-deep">
        {loading ? (
          <div className="flex h-48 items-center justify-center">
            <div className="h-8 w-8 animate-spin rounded-full border-2 border-electric border-t-transparent" />
          </div>
        ) : (
          <table className="w-full text-left text-sm">
            <thead>
              <tr className="border-b border-steel text-muted">
                <th className="px-6 py-3">ID</th>
                <th className="px-6 py-3">Payer</th>
                <th className="px-6 py-3">Payee</th>
                <th className="px-6 py-3">Amount</th>
                <th className="px-6 py-3">Status</th>
                <th className="px-6 py-3">Initiated</th>
              </tr>
            </thead>
            <tbody>
              {payments.map((p) => (
                <tr key={p.id} className="border-b border-steel/50 hover:bg-obsidian/50">
                  <td className="px-6 py-4">
                    <Link to={`/payments/${p.id}`} className="font-mono text-xs text-electric hover:text-shine">
                      {p.id.slice(0, 8)}
                    </Link>
                  </td>
                  <td className="px-6 py-4 text-body">{p.payerVpa}</td>
                  <td className="px-6 py-4 text-body">{p.payeeVpa}</td>
                  <td className="px-6 py-4 font-medium text-heading">{formatAmount(Number(p.amount))}</td>
                  <td className="px-6 py-4"><StatusBadge status={p.status} /></td>
                  <td className="px-6 py-4 text-muted">{new Date(p.initiatedAt).toLocaleString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  )
}
