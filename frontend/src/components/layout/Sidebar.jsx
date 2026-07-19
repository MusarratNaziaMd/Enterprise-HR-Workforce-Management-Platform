import { NavLink } from 'react-router-dom'
import { DashboardOutlined, PeopleOutlined, BusinessOutlined, AccessTimeOutlined, EventOutlined, EventBusyOutlined, PersonOutlined, ChevronLeft, ChevronRight, CloseOutlined, ApprovalOutlined } from '@mui/icons-material'
import { useRole } from '../../hooks/useRole'

export default function Sidebar({ collapsed, mobileOpen, onClose, onToggle }) {
  const { isAdmin, isHrManager, hasAuthority } = useRole()

  const navItems = [
    { to: '/', label: 'Dashboard', icon: DashboardOutlined },
    isAdmin && { to: '/employees', label: 'Employees', icon: PeopleOutlined },
    hasAuthority('DEPARTMENT_READ') && { to: '/departments', label: 'Departments', icon: BusinessOutlined },
    { to: '/profile', label: 'My Profile', icon: PersonOutlined },
    { to: '/attendance', label: 'My Attendance', icon: AccessTimeOutlined },
    { to: '/leaves', label: 'My Leaves', icon: EventOutlined },
    isHrManager && { to: '/leaves/approvals', label: 'Leave Approvals', icon: ApprovalOutlined },
  ].filter(Boolean)

  return (
    <>
      <div
        className={`fixed inset-0 z-30 bg-slate-950/40 transition-opacity md:hidden ${mobileOpen ? 'opacity-100' : 'pointer-events-none opacity-0'}`}
        onClick={onClose}
      />
      <aside
        className={`fixed inset-y-0 left-0 z-40 flex h-screen w-64 flex-col bg-slate-900 text-white shadow-xl transition-all duration-300 md:translate-x-0 ${collapsed ? 'md:w-[72px]' : 'md:w-64'} ${mobileOpen ? 'translate-x-0' : '-translate-x-full'}`}
      >
        <div className="flex h-16 items-center justify-between border-b border-white/5 px-4">
          <span className={`text-lg font-bold tracking-tight ${collapsed ? 'md:hidden' : ''}`}>HRpilot</span>
          <button onClick={onClose} className="p-2 text-white md:hidden hover:bg-slate-800 rounded-lg" aria-label="Close navigation">
            <CloseOutlined fontSize="small" />
          </button>
          <button onClick={onToggle} className="p-2 hidden text-slate-400 md:inline-flex hover:text-white rounded-lg hover:bg-slate-800" aria-label="Toggle sidebar">
            {collapsed ? <ChevronRight fontSize="small" /> : <ChevronLeft fontSize="small" />}
          </button>
        </div>

        <nav className="flex-1 space-y-1.5 overflow-y-auto px-3 py-6">
          {navItems.map(({ to, label, icon: Icon }) => (
            <NavLink
              key={to}
              to={to}
              end={to === '/'}
              onClick={onClose}
              className={({ isActive }) =>
                `flex items-center gap-3 rounded-lg px-3 py-3 text-sm font-medium transition-all ${isActive ? 'bg-blue-600 text-white shadow-lg shadow-blue-900/20' : 'text-slate-400 hover:bg-slate-800 hover:text-white'} ${collapsed ? 'md:justify-center' : ''}`
              }
              title={collapsed ? label : undefined}
            >
              <Icon fontSize="small" />
              <span className={collapsed ? 'md:hidden' : ''}>{label}</span>
            </NavLink>
          ))}
        </nav>
      </aside>
    </>
  )
}
