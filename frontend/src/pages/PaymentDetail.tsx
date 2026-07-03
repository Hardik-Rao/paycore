import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { ArrowLeft } from 'lucide-react'
import { api } from '../api/client'
import type { AuditLog, Payment } from '../types'
import StatusBadge from '../components/StatusBadge'

export default function PaymentDetail() {
  const { id } = useParams<{ id: string }>()
  const [payment, setPayment] = useState<Payment | null>(null)
  const [audit, setAudit] = useState<AuditLog[]>([])

  useEffect(() => {
    if (!id) return
    api.getPayment(id).then(setPayment).catch(console.error)
    api.getAuditTrail(id).then(setAudit).catch(console.error)
  }, [id])

  if (!payment) {
    return (
      <div className="flex h-64 items-center justify-center">
        <div className="h-8 w-8 animate-spin rounded-full border-2 border-electric border-t-transparent" />
      </div>
    )
  }

  const fmt = (n: number) =>
    new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' }).format(n)

  return (
    <div>
      <Link to="/payments" className="mb-6 inline-flex items-center gap-2 text-sm text-muted hover:text-electric">
        <ArrowLeft className="h-4 w-4" /> Back to payments
      </Link>

      <div className="mb-8 flex items-start justify-between">
        <div>
          <h1 className="text-3xl font-bold text-heading">Payment Detail</h1>
          <p className="mt-1 font-mono text-sm text-muted">{payment.id}</p>
        </div>
        <StatusBadge status={payment.status} />
      </div>

      <div className="mb-8 grid grid-cols-1 gap-6 md:grid-cols-2">
        <div className="glow-border rounded-xl bg-slate-deep p-6">
          <h2 className="mb-4 text-sm font-medium text-muted">Transaction</h2>
          <dl className="space-y-3 text-sm">
            <div className="flex justify-between"><dt className="text-muted">Payer</dt><dd className="text-heading">{payment.payerVpa}</dd></div>
            <div className="flex justify-between"><dt className="text-muted">Payee</dt><dd className="text-heading">{payment.payeeVpa}</dd></div>
            <div className="flex justify-between"><dt className="text-muted">Amount</dt><dd className="text-xl font-bold text-electric">{fmt(Number(payment.amount))}</dd></div>
            <div className="flex justify-between"><dt className="text-muted">Idempotency Key</dt><dd className="font-mono text-body">{payment.idempotencyKey}</dd></div>
          </dl>
        </div>
        <div className="glow-border rounded-xl bg-slate-deep p-6">
          <h2 className="mb-4 text-sm font-medium text-muted">Timeline</h2>
          <dl className="space-y-3 text-sm">
            <div className="flex justify-between"><dt className="text-muted">Initiated</dt><dd>{new Date(payment.initiatedAt).toLocaleString()}</dd></div>
            {payment.processedAt && (
              <div className="flex justify-between"><dt className="text-muted">Processed</dt><dd>{new Date(payment.processedAt).toLocaleString()}</dd></div>
            )}
            {payment.fraudScore != null && (
              <div className="flex justify-between"><dt className="text-muted">Fraud Score</dt><dd className="text-warning">{payment.fraudScore}</dd></div>
            )}
          </dl>
        </div>
      </div>

      <div className="glow-border rounded-xl bg-slate-deep p-6">
        <h2 className="mb-4 text-lg font-semibold text-heading">Audit Trail</h2>
        <div className="space-y-4">
          {audit.map((a) => (
            <div key={a.id} className="flex items-center gap-4 border-l-2 border-electric/40 pl-4">
              <div>
                <p className="text-sm text-heading">
                  {a.oldStatus ? `${a.oldStatus} ? ` : ''}{a.newStatus}
                </p>
                <p className="text-xs text-muted">{a.reason}   {new Date(a.changedAt).toLocaleString()}</p>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}
