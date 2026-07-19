import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import { AccessTimeOutlined, Login, Logout } from '@mui/icons-material'
import { DataTable, Pagination } from '../components/common/DataTable'
import { Badge } from '../components/common/Badge'
import LoadingSpinner from '../components/common/LoadingSpinner'
import EmptyState from '../components/common/EmptyState'
import { attendanceApi } from '../api/api'
import { useCurrentUserEmployee } from '../hooks/useCurrentUserEmployee'
import { useRole } from '../hooks/useRole'
import { Link } from 'react-router-dom'

export default function AttendancePage() {
  const [page, setPage] = useState(0)
  const queryClient = useQueryClient()
  const { employeeId, isLoading: loadingEmployee } = useCurrentUserEmployee()
  const { isAdmin } = useRole()

  const { data, isLoading } = useQuery({
    queryKey: ['attendance', employeeId, page],
    queryFn: () => attendanceApi.getEmployeeAttendance(employeeId, { page, size: 10 }).then((r) => r.data.data),
    enabled: !!employeeId,
  })

  const clockInMutation = useMutation({
    mutationFn: () => attendanceApi.clockIn({ employeeId }),
    onSuccess: () => { toast.success('Clocked in!'); queryClient.invalidateQueries({ queryKey: ['attendance'] }) },
    onError: (err) => toast.error(err.response?.data?.message || 'Clock-in failed'),
  })

  const clockOutMutation = useMutation({
    mutationFn: () => attendanceApi.clockOut({ employeeId }),
    onSuccess: () => { toast.success('Clocked out!'); queryClient.invalidateQueries({ queryKey: ['attendance'] }) },
    onError: (err) => toast.error(err.response?.data?.message || 'Clock-out failed'),
  })

  const columns = [
    { key: 'attendanceDate', label: 'Date', render: (row) => new Date(row.attendanceDate).toLocaleDateString() },
    {
      key: 'clockIn',
      label: 'Clock In',
      render: (row) => row.clockIn ? new Date(row.clockIn).toLocaleTimeString() : '—',
    },
    {
      key: 'clockOut',
      label: 'Clock Out',
      render: (row) => row.clockOut ? new Date(row.clockOut).toLocaleTimeString() : '—',
    },
    { key: 'status', label: 'Status', render: (row) => <Badge status={row.status} /> },
    { key: 'workHours', label: 'Hours', render: (row) => <span className="font-medium">{row.workHours}h</span> },
    { key: 'overtimeHours', label: 'OT', render: (row) => row.overtimeHours > 0 ? <span className="text-green-600 font-medium">+{row.overtimeHours}h</span> : '—' },
  ]

  if (loadingEmployee || isLoading) return <LoadingSpinner fullPage />

  if (!employeeId) {
    return (
      <div className="space-y-6 sm:space-y-8">
        <div className="page-header">
          <div>
            <h1 className="page-title">Attendance</h1>
            <p className="page-subtitle">Track daily attendance and work hours</p>
          </div>
        </div>
        <EmptyState
          icon={AccessTimeOutlined}
          title="No employee profile"
          description="Admin accounts don't have employee profiles. Attendance tracking requires an employee account."
          action={
            <Link to="/profile" className="btn-primary text-sm">View Profile</Link>
          }
        />
      </div>
    )
  }

  return (
    <div className="space-y-6 sm:space-y-8">
      <div className="page-header">
        <div>
          <h1 className="page-title">My Attendance</h1>
          <p className="page-subtitle">Track daily attendance and work hours</p>
        </div>
        <div className="flex gap-2">
          <button onClick={() => clockInMutation.mutate()} disabled={clockInMutation.isPending || !employeeId} className="btn-primary flex w-full items-center gap-2 text-sm sm:w-auto">
            <Login fontSize="small" /> Clock In
          </button>
          <button onClick={() => clockOutMutation.mutate()} disabled={clockOutMutation.isPending || !employeeId} className="btn-danger flex w-full items-center gap-2 text-sm sm:w-auto">
            <Logout fontSize="small" /> Clock Out
          </button>
        </div>
      </div>

      {data?.content?.length > 0 ? (
        <>
          <DataTable columns={columns} data={data.content} />
          <Pagination page={data.pageNumber} totalPages={data.totalPages} onPageChange={setPage} />
        </>
      ) : (
        <EmptyState icon={AccessTimeOutlined} title="No attendance records" description="Clock in to start tracking your attendance." />
      )}
    </div>
  )
}
