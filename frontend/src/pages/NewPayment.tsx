import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../api/client'
import type { Account } from '../types'

export default function NewPayment() {
  const navigate = useNavigate()
  const [accounts, setAccounts] = useState<Account[]>([])
  const [form, setForm] = useState({
    idempotencyKey: `pay-${Date.now()}`,
    payerVpa: '',
    payeeVpa: '',
    amount: '',
    currency: 'INR',
  })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    api.getAccounts().then(setAccounts).catch(console.error)
  }, [])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const payment = await api.createPayment({
        ...form,
        amount: parseFloat(form.amount),
      })
      navigate(`/payments/${payment.id}`)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Payment failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="max-w-xl">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-heading">New Payment</h1>
        <p className="mt-1 text-muted">Initiate a UPI-style transfer</p>
      </div>

      <form onSubmit={handleSubmit} className="glow-border space-y-5 rounded-xl bg-slate-deep p-8">
        <div>
          <label className="mb-1.5 block text-sm text-muted">Payer VPA</label>
          <select
            value={form.payerVpa}
            onChange={(e) => setForm({ ...form, payerVpa: e.target.value })}
            className="w-full rounded-lg border border-steel bg-obsidian px-4 py-2.5 text-heading outline-none focus:border-electric"
            required
          >
            <option value="">Select payer</option>
            {accounts.map((a) => (
              <option key={a.id} value={a.vpa}>{a.vpa}  {a.accountHolder}</option>
            ))}
          </select>
        </div>

        <div>
          <label className="mb-1.5 block text-sm text-muted">Payee VPA</label>
          <select
            value={form.payeeVpa}
            onChange={(e) => setForm({ ...form, payeeVpa: e.target.value })}
            className="w-full rounded-lg border border-steel bg-obsidian px-4 py-2.5 text-heading outline-none focus:border-electric"
            required
          >
            <option value="">Select payee</option>
            {accounts.filter((a) => a.vpa !== form.payerVpa).map((a) => (
              <option key={a.id} value={a.vpa}>{a.vpa}  {a.accountHolder}</option>
            ))}
          </select>
        </div>

        <div>
          <label className="mb-1.5 block text-sm text-muted">Amount (INR)</label>
          <input
            type="number"
            min="0.01"
            step="0.01"
            value={form.amount}
            onChange={(e) => setForm({ ...form, amount: e.target.value })}
            className="w-full rounded-lg border border-steel bg-obsidian px-4 py-2.5 text-heading outline-none focus:border-electric"
            placeholder="500.00"
            required
          />
        </div>

        {error && <p className="text-sm text-error">{error}</p>}

        <button type="submit" disabled={loading} className="btn-primary w-full disabled:opacity-50">
          {loading ? 'Processing' : 'Send Payment'}
        </button>
      </form>
    </div>
  )
}
