import type { PaymentStatus } from '../types'

const styles: Record<PaymentStatus, string> = {
  INITIATED: 'bg-pulse/15 text-pulse border-pulse/30',
  PROCESSING: 'bg-warning/15 text-warning border-warning/30',
  SUCCESS: 'bg-success/15 text-success border-success/30',
  FAILED: 'bg-error/15 text-error border-error/30',
  REVERSED: 'bg-reversed/15 text-reversed border-reversed/30',
  CANCELLED: 'bg-muted/15 text-muted border-muted/30',
}

export default function StatusBadge({ status }: { status: PaymentStatus | string }) {
  const key = status as PaymentStatus
  return (
    <span
      className={`inline-flex rounded-full border px-2.5 py-0.5 text-xs font-semibold uppercase tracking-wide ${styles[key] || styles.CANCELLED}`}
    >
      {status}
    </span>
  )
}
