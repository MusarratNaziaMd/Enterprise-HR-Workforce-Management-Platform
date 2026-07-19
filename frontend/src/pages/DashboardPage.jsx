import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import {
  PeopleOutlined,
  BusinessOutlined,
  EventOutlined,
  AccessTimeOutlined,
  PersonOutlined,
  TrendingUp,
  NotificationsOutlined,
  AssignmentTurnedInOutlined,
  CheckCircleOutline,
  CheckCircle,
  Cancel,
} from '@mui/icons-material'
import { Link } from 'react-router-dom'
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip } from 'recharts'
import StatCard from '../components/common/StatCard'
import { employeeApi, departmentApi, leaveApi, attendanceApi } from '../api/api'
import LoadingSpinner from '../components/common/LoadingSpinner'
import { useRole } from '../hooks/useRole'
import { useCurrentUserEmployee } from '../hooks/useCurrentUserEmployee'

export default function DashboardPage() {
  const { isAdmin, isHrManager, user } = useRole()
  const { employee } = useCurrentUserEmployee()
  const empId = employee?.id
  const queryClient = useQueryClient()

  const { data: empData, isLoading: loadingEmployees } = useQuery({
    queryKey: ['employees', { page: 0, size: 100 }],
    queryFn: () => employeeApi.getAll({ page: 0, size: 100 }).then((r) => r.data.data),
    enabled: isAdmin,
  })

  const { data: deptData, isLoading: loadingDepartments } = useQuery({
    queryKey: ['departments'],
    queryFn: () => departmentApi.getAll().then((r) => r.data.data),
    enabled: isAdmin,
  })

  const { data: pendingLeaves } = useQuery({
    queryKey: ['pendingLeaves'],
    queryFn: () => leaveApi.getAllPending().then((r) => r.data.data),
    enabled: isAdmin,
  })

  const approveMutation = useMutation({
    mutationFn: ({ id }) => leaveApi.approve(id, { approvedById: empId }),
    onSuccess: () => { toast.success('Leave approved'); queryClient.invalidateQueries({ queryKey: ['pendingLeaves'] }); queryClient.invalidateQueries({ queryKey: ['leaves'] }) },
    onError: (err) => toast.error(err.response?.data?.message || 'Failed to approve'),
  })

  const rejectMutation = useMutation({
    mutationFn: ({ id }) => leaveApi.reject(id, { approvedById: empId, rejectionReason: 'Rejected by manager' }),
    onSuccess: () => { toast.success('Leave rejected'); queryClient.invalidateQueries({ queryKey: ['pendingLeaves'] }); queryClient.invalidateQueries({ queryKey: ['leaves'] }) },
    onError: (err) => toast.error(err.response?.data?.message || 'Failed to reject'),
  })

  const { data: leaveBalance } = useQuery({
    queryKey: ['leaveBalance', empId],
    queryFn: () => leaveApi.getBalance(empId, new Date().getFullYear()).then((r) => r.data.data),
    enabled: !!empId,
  })

  const { data: recentAttendance } = useQuery({
    queryKey: ['attendance', empId],
    queryFn: () => attendanceApi.getEmployeeAttendance(empId, { size: 5 }).then((r) => r.data.data),
    enabled: !!empId,
  })

  if (loadingEmployees || loadingDepartments) return <LoadingSpinner fullPage />

  if (!isAdmin) {
    return (
      <div className="space-y-8">
        <div className="bg-white p-8 rounded-2xl shadow-sm border border-slate-100 flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-slate-900">Welcome back, {employee?.firstName || user?.username}</h1>
            <p className="text-slate-500 mt-1">{employee?.designation || 'Employee'}</p>
          </div>
          <div className="w-16 h-16 rounded-full bg-slate-200 flex items-center justify-center font-bold text-xl text-slate-600">
             {employee?.firstName?.[0]}{employee?.lastName?.[0]}
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <div className="lg:col-span-2 space-y-6">
            <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-100">
                <h3 className="text-lg font-bold text-slate-900 mb-4 flex items-center gap-2"><AssignmentTurnedInOutlined className="text-blue-600" /> Tasks</h3>
                <p className="text-slate-500">You have no pending tasks.</p>
            </div>
            
            <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-100">
                <h3 className="text-lg font-bold text-slate-900 mb-4 flex items-center gap-2"><AccessTimeOutlined className="text-emerald-600" /> Recent Attendance</h3>
                <div className="space-y-3">
                    {recentAttendance?.content?.map(att => (
                        <div key={att.id} className="flex justify-between p-3 bg-slate-50 rounded-lg text-sm">
                            <span>{att.attendanceDate}</span>
                            <span className="font-semibold">{att.status}</span>
                        </div>
                    ))}
                </div>
            </div>
          </div>
          
          <div className="space-y-6">
            <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-100">
                <h3 className="text-lg font-bold text-slate-900 mb-4 flex items-center gap-2"><EventOutlined className="text-amber-600" /> Leave Balance</h3>
                <div className="space-y-2">
                    {leaveBalance?.map(lb => (
                        <div key={lb.leaveTypeName} className="flex justify-between text-sm">
                            <span className="text-slate-500">{lb.leaveTypeName}</span>
                            <span className="font-semibold">{lb.remainingDays} / {lb.totalDays}</span>
                        </div>
                    ))}
                </div>
            </div>
            
            <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-100">
                <h3 className="text-lg font-bold text-slate-900 mb-4 flex items-center gap-2"><NotificationsOutlined className="text-violet-600" /> Announcements</h3>
                <p className="text-sm text-slate-600">No new announcements.</p>
            </div>
          </div>
        </div>
      </div>
    )
  }

  const totalEmployees = empData?.totalElements || 0
  const activeEmployees = empData?.content?.filter((e) => e.status === 'ACTIVE').length || 0
  const onLeave = empData?.content?.filter((e) => e.status === 'ON_LEAVE').length || 0
  
  const chartData = [
    { name: 'Active', value: activeEmployees },
    { name: 'On Leave', value: onLeave },
    { name: 'Other', value: totalEmployees - activeEmployees - onLeave },
  ]
  const COLORS = ['#10b981', '#f59e0b', '#64748b']

  return (
    <div className="space-y-8">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-slate-900">Admin Dashboard</h1>
        <div className="flex gap-2">
            <button className="px-4 py-2 bg-white border border-slate-200 rounded-lg text-sm font-medium hover:bg-slate-50">Reports</button>
            <Link to="/employees" className="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm font-medium hover:bg-blue-700">Add Employee</Link>
        </div>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        <StatCard title="Total Employees" value={totalEmployees} icon={PeopleOutlined} color="primary" />
        <StatCard title="Active Workforce" value={activeEmployees} icon={TrendingUp} color="success" />
        <StatCard title="Departments" value={deptData?.length || 0} icon={BusinessOutlined} color="purple" />
        <StatCard title="On Leave Today" value={onLeave} icon={EventOutlined} color="warning" />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-100 lg:col-span-1">
            <h3 className="text-lg font-bold text-slate-900 mb-4">Workforce Status</h3>
            <div className="h-64">
                <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                        <Pie data={chartData} innerRadius={60} outerRadius={80} paddingAngle={5} dataKey="value">
                            {chartData.map((entry, index) => <Cell key={index} fill={COLORS[index % COLORS.length]} />)}
                        </Pie>
                        <Tooltip />
                    </PieChart>
                </ResponsiveContainer>
            </div>
        </div>

        <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-100 lg:col-span-2">
          <div className="flex items-center justify-between mb-6">
            <h3 className="text-lg font-bold text-slate-900">Leave Approvals Pending</h3>
            {pendingLeaves?.length > 0 && (
              <Link to="/leaves/approvals" className="text-sm font-medium text-blue-600 hover:text-blue-700">View All</Link>
            )}
          </div>
          <div className="space-y-4">
              {pendingLeaves?.length > 0 ? pendingLeaves.slice(0, 5).map((leave) => (
                <div key={leave.id} className="p-4 rounded-xl bg-slate-50 border border-slate-100 flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-full bg-blue-100 text-blue-700 flex items-center justify-center font-bold text-xs">
                      {leave.employeeName?.split(' ').map(n => n[0]).join('')}
                    </div>
                    <div>
                      <p className="text-sm font-semibold text-slate-900">{leave.employeeName}</p>
                      <p className="text-xs text-slate-500">{leave.leaveTypeName} ({leave.totalDays} day{leave.totalDays !== 1 ? 's' : ''})</p>
                    </div>
                  </div>
                  {isHrManager && (
                    <div className="flex gap-2">
                      <button
                        onClick={() => approveMutation.mutate({ id: leave.id })}
                        disabled={!empId || approveMutation.isPending}
                        className="text-xs text-emerald-600 font-medium px-3 py-1 bg-emerald-50 rounded-md hover:bg-emerald-100 disabled:opacity-50"
                      >
                        <CheckCircle fontSize="inherit" className="mr-1" />Approve
                      </button>
                      <button
                        onClick={() => rejectMutation.mutate({ id: leave.id })}
                        disabled={!empId || rejectMutation.isPending}
                        className="text-xs text-rose-600 font-medium px-3 py-1 bg-rose-50 rounded-md hover:bg-rose-100 disabled:opacity-50"
                      >
                        <Cancel fontSize="inherit" className="mr-1" />Reject
                      </button>
                    </div>
                  )}
                </div>
              )) : (
                <p className="text-sm text-slate-500 text-center py-4">No pending leave approvals.</p>
              )}
          </div>
        </div>
      </div>
    </div>
  )
}
