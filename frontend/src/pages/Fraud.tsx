import { useEffect, useState } from 'react'
import { api } from '../api/client'
import type { FraudAlert } from '../types'

export default function Fraud() {
  const [alerts, setAlerts] = useState<FraudAlert[]>([])
  const [loading, setLoading] = useState(true)

  const load = () => {
    api.getFraudAlerts()
      .then((r) => setAlerts(r.content))
      .catch(console.error)
      .finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  const review = async (id: string, resolution: string) => {
    await api.reviewFraudAlert(id, resolution)
    load()
  }

  return (
    <div>
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-heading">Fraud Queue</h1>
        <p className="mt-1 text-muted">Review flagged transactions</p>
      </div>

      <div className="glow-border rounded-xl bg-slate-deep">
        {loading ? (
          <div className="flex h-48 items-center justify-center">
            <div className="h-8 w-8 animate-spin rounded-full border-2 border-electric border-t-transparent" />
          </div>
        ) : alerts.length === 0 ? (
          <p className="px-6 py-16 text-center text-muted">No pending fraud alerts</p>
        ) : (
          <div className="divide-y divide-steel">
            {alerts.map((a) => (
              <div key={a.id} className="flex items-center justify-between px-6 py-5">
                <div>
                  <p className="font-mono text-sm text-electric">Payment: {a.paymentId.slice(0, 8)}</p>
                  <p className="mt-1 text-sm text-body">
                    Score: <span className="font-bold text-warning">{a.fraudScore}</span>
                    {'   '}{a.actionTaken}
                  </p>
                  <p className="mt-1 text-xs text-muted">
                    Rules: {a.triggeredRules?.join(', ') || ''}
                  </p>
                </div>
                <div className="flex gap-2">
                  <button
                    onClick={() => review(a.id, 'APPROVED')}
                    className="rounded-lg border border-success/30 bg-success/10 px-4 py-2 text-sm text-success hover:bg-success/20"
                  >
                    Approve
                  </button>
                  <button
                    onClick={() => review(a.id, 'REJECTED')}
                    className="rounded-lg border border-error/30 bg-error/10 px-4 py-2 text-sm text-error hover:bg-error/20"
                  >
                    Reject
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
