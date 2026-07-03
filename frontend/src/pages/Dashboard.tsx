import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { IndianRupee, TrendingUp, CheckCircle, XCircle } from 'lucide-react'
import { api } from '../api/client'
import type { Payment, PaymentStats } from '../types'
import StatCard from '../components/StatCard'
import StatusBadge from '../components/StatusBadge'

function formatAmount(n: number) {
  return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' }).format(n)
}

export default function Dashboard() {
  const [stats, setStats] = useState<PaymentStats | null>(null)
  const [recent, setRecent] = useState<Payment[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const from = new Date(Date.now() - 86400000).toISOString()
    const to = new Date().toISOString()
    Promise.all([api.getStats(from, to), api.getPayments(0, 8)])
      .then(([s, p]) => {
        setStats(s)
        setRecent(p.content)
      })
      .catch(console.error)
      .finally(() => setLoading(false))
  }, [])

  if (loading) {
    return (
      <div className="flex h-64 items-center justify-center">
        <div className="h-8 w-8 animate-spin rounded-full border-2 border-electric border-t-transparent" />
      </div>
    )
  }

  return (
    <div>
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-heading">Dashboard</h1>
        <p className="mt-1 text-muted">Real-time payment operations overview</p>
      </div>

      <div className="mb-8 grid grid-cols-1 gap-6 md:grid-cols-2 xl:grid-cols-4">
        <StatCard
          label="Total Volume (24h)"
          value={stats ? formatAmount(Number(stats.totalVolume)) : ''}
          icon={IndianRupee}
          accent
        />
        <StatCard
          label="Success Rate"
          value={stats ? `${stats.successRate.toFixed(1)}%` : ''}
          sub={`${stats?.successCount ?? 0} successful`}
          icon={TrendingUp}
        />
        <StatCard
          label="Successful"
          value={String(stats?.successCount ?? 0)}
          icon={CheckCircle}
        />
        <StatCard
          label="Failed"
          value={String(stats?.failedCount ?? 0)}
          icon={XCircle}
        />
      </div>

      <div className="glow-border rounded-xl bg-slate-deep">
        <div className="flex items-center justify-between border-b border-steel px-6 py-4">
          <h2 className="text-lg font-semibold text-heading">Recent Transactions</h2>
          <Link to="/payments" className="text-sm text-electric hover:text-shine">
            View all ?
          </Link>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead>
              <tr className="border-b border-steel text-muted">
                <th className="px-6 py-3 font-medium">Payer ? Payee</th>
                <th className="px-6 py-3 font-medium">Amount</th>
                <th className="px-6 py-3 font-medium">Status</th>
                <th className="px-6 py-3 font-medium">Time</th>
              </tr>
            </thead>
            <tbody>
              {recent.length === 0 ? (
                <tr>
                  <td colSpan={4} className="px-6 py-12 text-center text-muted">
                    No payments yet.{' '}
                    <Link to="/payments/new" className="text-electric">
                      Create one
                    </Link>
                  </td>
                </tr>
              ) : (
                recent.map((p) => (
                  <tr key={p.id} className="border-b border-steel/50 hover:bg-obsidian/50">
                    <td className="px-6 py-4">
                      <Link to={`/payments/${p.id}`} className="text-heading hover:text-electric">
                        {p.payerVpa} ? {p.payeeVpa}
                      </Link>
                    </td>
                    <td className="px-6 py-4 font-medium text-heading">
                      {formatAmount(Number(p.amount))}
                    </td>
                    <td className="px-6 py-4">
                      <StatusBadge status={p.status} />
                    </td>
                    <td className="px-6 py-4 text-muted">
                      {new Date(p.initiatedAt).toLocaleString()}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}
