import { Routes, Route, Navigate } from 'react-router-dom'
import { useAuth } from './hooks/useAuth'
import { useRole } from './hooks/useRole'
import DashboardLayout from './components/layout/DashboardLayout'
import LoginPage from './pages/LoginPage'
import DashboardPage from './pages/DashboardPage'
import EmployeesPage from './pages/EmployeesPage'
import DepartmentsPage from './pages/DepartmentsPage'
import AttendancePage from './pages/AttendancePage'
import LeavesPage from './pages/LeavesPage'
import ProfilePage from './pages/ProfilePage'
import LeaveApprovalsPage from './pages/LeaveApprovalsPage'

function ProtectedRoute({ children }) {
  const { user, loading } = useAuth()
  if (loading) {
    return (
      <div className="h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600" />
      </div>
    )
  }
  return user ? children : <Navigate to="/login" replace />
}

function AdminRoute({ children, authority }) {
  const { user, loading } = useAuth()
  const { hasAuthority } = useRole()
  if (loading) {
    return (
      <div className="h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600" />
      </div>
    )
  }
  if (!user) return <Navigate to="/login" replace />
  if (authority && !hasAuthority(authority)) return <Navigate to="/" replace />
  return children
}

function PublicRoute({ children }) {
  const { user, loading } = useAuth()
  if (loading) return null
  return user ? <Navigate to="/" replace /> : children
}

export default function App() {
  return (
    <Routes>
      <Route
        path="/login"
        element={
          <PublicRoute>
            <LoginPage />
          </PublicRoute>
        }
      />
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <DashboardLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<DashboardPage />} />
        <Route path="employees" element={<AdminRoute authority="EMPLOYEE_READ_ALL"><EmployeesPage /></AdminRoute>} />
        <Route path="departments" element={<AdminRoute authority="DEPARTMENT_READ"><DepartmentsPage /></AdminRoute>} />
        <Route path="attendance" element={<AttendancePage />} />
        <Route path="leaves" element={<LeavesPage />} />
        <Route path="profile" element={<ProfilePage />} />
        <Route path="leaves/approvals" element={<AdminRoute authority="LEAVE_APPROVE"><LeaveApprovalsPage /></AdminRoute>} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
