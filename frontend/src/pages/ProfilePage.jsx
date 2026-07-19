import { useAuth } from '../hooks/useAuth'
import { useCurrentUserEmployee } from '../hooks/useCurrentUserEmployee'
import LoadingSpinner from '../components/common/LoadingSpinner'
import { Badge } from '../components/common/Badge'
import { CalendarToday, EventBusy, AccessTime } from '@mui/icons-material'
import { Link } from 'react-router-dom'

function roleLabel(user) {
  if (user?.portal === 'ADMIN') return 'HR Admin'
  if (user?.authorities?.includes('LEAVE_APPROVE')) return 'Manager'
  return 'Employee'
}

export default function ProfilePage() {
  const { user } = useAuth()
  const { employee, isLoading: loadingEmployee } = useCurrentUserEmployee()

  if (loadingEmployee) return <LoadingSpinner fullPage />

  const initials = employee
    ? `${employee.firstName?.[0] || ''}${employee.lastName?.[0] || ''}`
    : user?.username?.[0]?.toUpperCase() || '?'

  const displayName = employee?.fullName || user?.username || '—'
  const subtitle = employee
    ? [employee.designation, employee.departmentName].filter(Boolean).join(' \u00B7 ')
    : user?.email

  return (
    <div className="space-y-6">
      <div>
        <h1 className="page-title">My Profile</h1>
        <p className="page-subtitle">{employee ? 'Your employee information' : 'Your account information'}</p>
      </div>

      <div className="card p-6 sm:p-8">
        <div className="flex flex-col sm:flex-row items-start gap-6">
          <div className="w-20 h-20 bg-primary-100 text-primary-700 rounded-2xl flex items-center justify-center text-2xl font-bold shrink-0">
            {initials}
          </div>
          <div className="flex-1">
            <h2 className="text-2xl font-bold text-gray-900">{displayName}</h2>
            {subtitle && <p className="text-sm text-gray-500 mt-1">{subtitle}</p>}
            <div className="flex flex-wrap gap-2 mt-3">
              {employee?.status && <Badge status={employee.status} />}
              <span className="badge bg-slate-100 text-slate-700">{roleLabel(user)}</span>
              {employee?.employmentType && (
                <span className="badge bg-slate-100 text-slate-700">{employee.employmentType.replace(/_/g, ' ')}</span>
              )}
              {employee?.employeeCode && (
                <span className="badge bg-slate-100 text-slate-700">{employee.employeeCode}</span>
              )}
            </div>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="card divide-y divide-slate-100">
          <div className="px-6 py-4">
            <h3 className="text-sm font-semibold text-gray-900">Account Details</h3>
          </div>
          <InfoRow label="Username" value={user?.username} />
          <InfoRow label="Email" value={user?.email} />
          <InfoRow label="Portal" value={user?.portal === 'ADMIN' ? 'Admin Portal' : 'Employee Portal'} />
          <InfoRow label="Role" value={roleLabel(user)} />
          {employee && (
            <>
              <InfoRow label="Employee Code" value={employee.employeeCode} />
              <InfoRow label="Designation" value={employee.designation} />
            </>
          )}
        </div>

        <div className="card divide-y divide-slate-100">
          <div className="px-6 py-4">
            <h3 className="text-sm font-semibold text-gray-900">{employee ? 'Work Details' : 'Permissions'}</h3>
          </div>
          {employee ? (
            <>
              <InfoRow label="Department" value={employee.departmentName} />
              <InfoRow label="Manager" value={employee.managerName || '\u2014'} />
              <InfoRow label="Employment Type" value={employee.employmentType?.replace(/_/g, ' ')} />
              <InfoRow label="Status" value={employee.status} isStatus />
              <InfoRow label="Date of Joining" value={employee.dateOfJoining ? new Date(employee.dateOfJoining).toLocaleDateString() : '\u2014'} />
              <InfoRow label="Probation End" value={employee.probationEndDate ? new Date(employee.probationEndDate).toLocaleDateString() : '\u2014'} />
              <InfoRow label="Phone" value={employee.phone || '\u2014'} />
              <InfoRow
                label="Address"
                value={[employee.address, employee.city, employee.state, employee.country].filter(Boolean).join(', ') || '\u2014'}
              />
            </>
          ) : (
            user?.authorities?.map((auth) => (
              <div key={auth} className="flex items-center justify-between px-6 py-2">
                <span className="text-xs font-mono text-gray-500">{auth}</span>
                <span className="text-xs text-green-600 font-medium">Granted</span>
              </div>
            ))
          )}
        </div>
      </div>

      {!employee && (
        <div className="card p-6 text-center">
          <p className="text-sm text-gray-500">
            No employee profile is linked to this account. Employee profiles are created by HR administrators.
          </p>
        </div>
      )}

      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <Link to="/attendance" className="card p-5 flex items-center gap-4 hover:shadow-md transition-shadow">
          <div className="w-10 h-10 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center">
            <AccessTime fontSize="small" />
          </div>
          <div>
            <p className="text-sm font-semibold text-gray-900">My Attendance</p>
            <p className="text-xs text-gray-400">View and clock in/out</p>
          </div>
        </Link>
        <Link to="/leaves" className="card p-5 flex items-center gap-4 hover:shadow-md transition-shadow">
          <div className="w-10 h-10 rounded-xl bg-yellow-50 text-yellow-600 flex items-center justify-center">
            <EventBusy fontSize="small" />
          </div>
          <div>
            <p className="text-sm font-semibold text-gray-900">My Leaves</p>
            <p className="text-xs text-gray-400">Apply and track leaves</p>
          </div>
        </Link>
        <div className="card p-5 flex items-center gap-4">
          <div className="w-10 h-10 rounded-xl bg-green-50 text-green-600 flex items-center justify-center">
            <CalendarToday fontSize="small" />
          </div>
          <div>
            <p className="text-sm font-semibold text-gray-900">Joined</p>
            <p className="text-xs text-gray-400">{employee?.dateOfJoining ? new Date(employee.dateOfJoining).toLocaleDateString() : 'N/A'}</p>
          </div>
        </div>
      </div>
    </div>
  )
}

function InfoRow({ label, value, isStatus }) {
  return (
    <div className="flex items-center justify-between px-6 py-3">
      <span className="text-sm text-gray-500">{label}</span>
      {isStatus ? (
        <Badge status={value} />
      ) : (
        <span className="text-sm font-medium text-gray-900">{value || '\u2014'}</span>
      )}
    </div>
  )
}
