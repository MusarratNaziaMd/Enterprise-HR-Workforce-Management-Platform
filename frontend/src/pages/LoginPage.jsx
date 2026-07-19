import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'

const DEMO_USERS = [
  { label: 'HR Manager', username: 'priya.sharma', password: 'Password@1', portal: 'ADMIN', color: 'bg-purple-500' },
  { label: 'Employee', username: 'amit.kumar', password: 'Password@1', portal: 'EMPLOYEE', color: 'bg-green-500' },
]

export default function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [portal, setPortal] = useState('ADMIN')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [showPassword, setShowPassword] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    if (!email.trim() || !password) {
      setError('Please enter both email and password.')
      return
    }
    setLoading(true)
    try {
      await login(email.trim(), password, portal, true)
      navigate('/', { replace: true })
    } catch (err) {
      setError(err.response?.data?.message || 'Invalid email or password.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 p-4">
      <div className="w-full max-w-md bg-white rounded-2xl shadow-xl p-8 border border-gray-100">
        <div className="text-center mb-8">
          <div className="w-12 h-12 bg-blue-600 rounded-xl flex items-center justify-center mx-auto mb-4 shadow-lg shadow-blue-200">
            <svg viewBox="0 0 48 48" className="w-7 h-7 text-white">
              <path d="M14 34V14h4.5l5.5 12 5.5-12H34v20h-3V18.5L26 30h-4L17 18.5V34h-3z" fill="currentColor" />
            </svg>
          </div>
          <h1 className="text-2xl font-bold text-gray-900">HRpilot</h1>
          <p className="text-gray-500 text-sm mt-1">Enterprise HR Management</p>
        </div>

        {error && (
          <div className="mb-6 p-3 text-sm text-red-700 bg-red-50 rounded-lg border border-red-100">
            {error}
          </div>
        )}

        {/* Portal Selector */}
        <div className="mb-5">
          <label className="block text-sm font-medium text-gray-700 mb-2">Login as</label>
          <div className="grid grid-cols-2 gap-2">
            {['ADMIN', 'EMPLOYEE'].map((p) => (
              <button
                key={p}
                type="button"
                onClick={() => setPortal(p)}
                disabled={loading}
                className={`py-2.5 rounded-lg text-sm font-semibold border-2 transition-all ${
                  portal === p
                    ? 'border-blue-600 bg-blue-50 text-blue-700'
                    : 'border-gray-200 bg-white text-gray-500 hover:border-gray-300 hover:bg-gray-50'
                }`}
              >
                {p === 'ADMIN' ? 'Admin' : 'Employee'}
              </button>
            ))}
          </div>
        </div>

        <form onSubmit={handleSubmit} className="space-y-5">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Email or Username</label>
            <input
              type="text"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full px-4 py-2.5 rounded-lg border border-gray-300 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all"
              placeholder="you@company.com"
              disabled={loading}
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Password</label>
            <div className="relative">
              <input
                type={showPassword ? 'text' : 'password'}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="w-full px-4 py-2.5 rounded-lg border border-gray-300 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all"
                placeholder="••••••••"
                disabled={loading}
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute right-3 top-2.5 text-gray-400 hover:text-gray-600"
              >
                {showPassword ? 'Hide' : 'Show'}
              </button>
            </div>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold py-2.5 rounded-lg transition-colors flex items-center justify-center"
          >
            {loading ? 'Signing in...' : 'Sign in'}
          </button>
        </form>

        <div className="mt-8 pt-6 border-t border-gray-100">
          <p className="text-xs text-gray-400 text-center mb-3">Quick Login (Demo)</p>
          <div className="grid grid-cols-2 gap-2">
            {DEMO_USERS.map((u) => (
              <button
                key={u.label}
                type="button"
                onClick={() => { setEmail(u.username); setPassword(u.password); setPortal(u.portal) }}
                className="flex items-center gap-2 px-3 py-2 rounded-lg border border-gray-200 hover:border-blue-300 hover:bg-blue-50 text-xs font-medium text-gray-600 transition-all"
              >
                <span className={`w-2 h-2 rounded-full ${u.color}`} />
                {u.label}
              </button>
            ))}
          </div>
        </div>
      </div>
    </div>
  )
}
