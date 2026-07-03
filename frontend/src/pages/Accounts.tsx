import { useEffect, useState } from 'react'
import { api } from '../api/client'
import type { Account } from '../types'

export default function Accounts() {
  const [accounts, setAccounts] = useState<Account[]>([])
  const [loading, setLoading] = useState(true)
  const [form, setForm] = useState({ vpa: '', accountHolder: '', accountType: 'INDIVIDUAL' })
  const [error, setError] = useState('')
  const [balances, setBalances] = useState<Record<string, number>>({})

  const load = () => {
    api.getAccounts()
      .then(async (accs) => {
        setAccounts(accs)
        const bals: Record<string, number> = {}
        await Promise.all(
          accs.map(async (a) => {
            const b = await api.getBalance(a.vpa)
            if (b) bals[a.vpa] = Number(b.balance)
          })
        )
        setBalances(bals)
      })
      .catch(console.error)
      .finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    try {
      await api.createAccount(form)
      setForm({ vpa: '', accountHolder: '', accountType: 'INDIVIDUAL' })
      load()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create account')
    }
  }

  const fmt = (n: number) =>
    new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' }).format(n)

  return (
    <div>
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-heading">Accounts</h1>
        <p className="mt-1 text-muted">Manage VPAs and view balances</p>
      </div>

      <div className="mb-8 glow-border rounded-xl bg-slate-deep p-6">
        <h2 className="mb-4 text-lg font-semibold text-heading">Create Account</h2>
        <form onSubmit={handleCreate} className="grid grid-cols-1 gap-4 md:grid-cols-4">
          <input
            placeholder="VPA (e.g. alice@paycore)"
            value={form.vpa}
            onChange={(e) => setForm({ ...form, vpa: e.target.value })}
            className="rounded-lg border border-steel bg-obsidian px-4 py-2.5 text-heading outline-none focus:border-electric"
            required
          />
          <input
            placeholder="Account holder name"
            value={form.accountHolder}
            onChange={(e) => setForm({ ...form, accountHolder: e.target.value })}
            className="rounded-lg border border-steel bg-obsidian px-4 py-2.5 text-heading outline-none focus:border-electric"
            required
          />
          <select
            value={form.accountType}
            onChange={(e) => setForm({ ...form, accountType: e.target.value })}
            className="rounded-lg border border-steel bg-obsidian px-4 py-2.5 text-heading outline-none focus:border-electric"
          >
            <option value="INDIVIDUAL">Individual</option>
            <option value="MERCHANT">Merchant</option>
            <option value="WALLET">Wallet</option>
          </select>
          <button type="submit" className="btn-primary">Create Account</button>
        </form>
        {error && <p className="mt-2 text-sm text-error">{error}</p>}
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
                <th className="px-6 py-3">VPA</th>
                <th className="px-6 py-3">Holder</th>
                <th className="px-6 py-3">Type</th>
                <th className="px-6 py-3">Balance</th>
                <th className="px-6 py-3">Status</th>
              </tr>
            </thead>
            <tbody>
              {accounts.map((a) => (
                <tr key={a.id} className="border-b border-steel/50 hover:bg-obsidian/50">
                  <td className="px-6 py-4 font-medium text-electric">{a.vpa}</td>
                  <td className="px-6 py-4 text-body">{a.accountHolder}</td>
                  <td className="px-6 py-4 text-muted">{a.accountType}</td>
                  <td className="px-6 py-4 font-medium text-heading">
                    {balances[a.vpa] != null ? fmt(balances[a.vpa]) : ''}
                  </td>
                  <td className="px-6 py-4">
                    <span className={a.active ? 'text-success' : 'text-error'}>
                      {a.active ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  )
}
