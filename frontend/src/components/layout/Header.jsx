import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'
import { MenuOutlined, PersonOutlined, LogoutOutlined, KeyboardArrowDown } from '@mui/icons-material'

export default function Header({ onMenuToggle }) {
  const { user, logout } = useAuth()
  const [dropdownOpen, setDropdownOpen] = useState(false)
  return (
    <header className="sticky top-0 z-30 flex h-16 items-center justify-between border-b border-slate-200 bg-white/95 px-4 backdrop-blur sm:px-6">
      <button onClick={onMenuToggle} className="icon-button md:hidden" aria-label="Open navigation"><MenuOutlined fontSize="small" /></button>
      <div className="relative ml-auto">
        <button onClick={() => setDropdownOpen(!dropdownOpen)} className="flex items-center gap-2 rounded-xl px-2 py-1.5 transition-colors hover:bg-slate-50 sm:gap-3 sm:px-3">
          <div className="flex h-9 w-9 items-center justify-center rounded-full bg-primary-100 text-primary-700"><PersonOutlined fontSize="small" /></div>
          <div className="hidden text-left sm:block"><p className="text-sm font-semibold text-slate-700">{user?.username}</p><p className="max-w-44 truncate text-xs text-slate-400">{user?.email}</p></div>
          <KeyboardArrowDown fontSize="small" className="hidden text-slate-400 sm:block" />
        </button>
        {dropdownOpen && (
          <>
            <div className="fixed inset-0 z-40" onClick={() => setDropdownOpen(false)} />
            <div className="absolute right-0 z-50 mt-2 w-52 rounded-xl border border-slate-200 bg-white py-1.5 shadow-lg">
              <Link
                to="/profile"
                onClick={() => setDropdownOpen(false)}
                className="flex w-full items-center gap-2 px-4 py-2.5 text-sm font-medium text-slate-700 transition-colors hover:bg-slate-50"
              >
                <PersonOutlined fontSize="small" />My Profile
              </Link>
              <div className="mx-3 my-1 border-t border-slate-100" />
              <button onClick={logout} className="flex w-full items-center gap-2 px-4 py-2.5 text-sm font-medium text-red-600 transition-colors hover:bg-red-50">
                <LogoutOutlined fontSize="small" />Sign out
              </button>
            </div>
          </>
        )}
      </div>
    </header>
  )
}
