import { NavLink, Outlet } from 'react-router-dom'
import {
  LayoutDashboard,
  ArrowLeftRight,
  Users,
  ShieldAlert,
  Zap,
  Plus,
} from 'lucide-react'

const nav = [
  { to: '/', icon: LayoutDashboard, label: 'Dashboard' },
  { to: '/payments', icon: ArrowLeftRight, label: 'Payments' },
  { to: '/payments/new', icon: Plus, label: 'New Payment' },
  { to: '/accounts', icon: Users, label: 'Accounts' },
  { to: '/fraud', icon: ShieldAlert, label: 'Fraud Queue' },
]

export default function Layout() {
  return (
    <div className="flex min-h-screen bg-void">
      <aside className="fixed left-0 top-0 flex h-full w-64 flex-col border-r border-steel bg-obsidian">
        <div className="accent-line absolute right-0 top-0 h-full w-px" />
        <div className="flex items-center gap-3 border-b border-steel px-6 py-5">
          <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-gradient-to-br from-electric to-royal shadow-[0_0_20px_rgba(0,180,255,0.4)]">
            <Zap className="h-5 w-5 text-white" />
          </div>
          <div>
            <h1 className="text-lg font-bold text-heading">PayCore</h1>
            <p className="text-xs text-muted">Payment Infrastructure</p>
          </div>
        </div>
        <nav className="flex-1 space-y-1 p-4">
          {nav.map(({ to, icon: Icon, label }) => (
            <NavLink
              key={to}
              to={to}
              end={to === '/'}
              className={({ isActive }) =>
                `flex items-center gap-3 rounded-lg px-4 py-3 text-sm font-medium transition-all ${
                  isActive
                    ? 'bg-slate-deep text-electric shadow-[inset_0_0_20px_rgba(0,180,255,0.08)] border border-electric/20'
                    : 'text-body hover:bg-slate-deep hover:text-shine'
                }`
              }
            >
              <Icon className="h-4 w-4" />
              {label}
            </NavLink>
          ))}
        </nav>
        <div className="border-t border-steel p-4">
          <div className="rounded-lg bg-slate-deep p-3 glow-border">
            <p className="text-xs text-muted">System Status</p>
            <p className="mt-1 flex items-center gap-2 text-sm text-success">
              <span className="h-2 w-2 animate-pulse rounded-full bg-success" />
              All services online
            </p>
          </div>
        </div>
      </aside>
      <main className="ml-64 flex-1 p-8">
        <Outlet />
      </main>
    </div>
  )
}
