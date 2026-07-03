import type { LucideIcon } from 'lucide-react'

interface StatCardProps {
  label: string
  value: string
  sub?: string
  icon: LucideIcon
  accent?: boolean
}

export default function StatCard({ label, value, sub, icon: Icon, accent }: StatCardProps) {
  return (
    <div className="glow-border rounded-xl bg-slate-deep p-6 transition-all">
      <div className="flex items-start justify-between">
        <div>
          <p className="text-sm text-muted">{label}</p>
          <p className={`mt-2 text-3xl font-bold ${accent ? 'text-electric' : 'text-heading'}`}>
            {value}
          </p>
          {sub && <p className="mt-1 text-xs text-muted">{sub}</p>}
        </div>
        <div className="rounded-lg bg-obsidian p-3">
          <Icon className={`h-5 w-5 ${accent ? 'text-electric' : 'text-pulse'}`} />
        </div>
      </div>
    </div>
  )
}
