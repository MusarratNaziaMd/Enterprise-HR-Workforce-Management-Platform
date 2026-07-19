import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import { CheckCircle, Cancel, EventBusyOutlined } from '@mui/icons-material'
import { DataTable } from '../components/common/DataTable'
import LoadingSpinner from '../components/common/LoadingSpinner'
import EmptyState from '../components/common/EmptyState'
import { leaveApi } from '../api/api'
import { useCurrentUserEmployee } from '../hooks/useCurrentUserEmployee'

export default function LeaveApprovalsPage() {
  const queryClient = useQueryClient()
  const { employeeId, isLoading: loadingEmployee } = useCurrentUserEmployee()

  const { data: pendingLeaves, isLoading } = useQuery({
    queryKey: ['pendingLeaves'],
    queryFn: () => leaveApi.getAllPending().then((r) => r.data.data),
  })

  const approveMutation = useMutation({
    mutationFn: ({ id }) => leaveApi.approve(id, { approvedById: employeeId }),
    onSuccess: () => { toast.success('Leave approved'); queryClient.invalidateQueries({ queryKey: ['pendingLeaves'] }); queryClient.invalidateQueries({ queryKey: ['leaves'] }) },
    onError: (err) => toast.error(err.response?.data?.message || 'Failed to approve'),
  })

  const rejectMutation = useMutation({
    mutationFn: ({ id }) => leaveApi.reject(id, { approvedById: employeeId, rejectionReason: 'Rejected by manager' }),
    onSuccess: () => { toast.success('Leave rejected'); queryClient.invalidateQueries({ queryKey: ['pendingLeaves'] }); queryClient.invalidateQueries({ queryKey: ['leaves'] }) },
    onError: (err) => toast.error(err.response?.data?.message || 'Failed to reject'),
  })

  const columns = [
    {
      key: 'employeeName',
      label: 'Employee',
      render: (row) => (
        <div>
          <p className="font-medium text-gray-900">{row.employeeName}</p>
          <p className="text-xs text-gray-500">{row.employeeCode}</p>
        </div>
      ),
    },
    {
      key: 'leaveTypeName',
      label: 'Type',
      render: (row) => (
        <div>
          <p className="font-medium text-gray-900">{row.leaveTypeName}</p>
          <p className="text-xs text-gray-500">{row.leaveTypeCode}</p>
        </div>
      ),
    },
    {
      key: 'dates',
      label: 'Dates',
      render: (row) => (
        <span className="text-sm">
          {new Date(row.startDate).toLocaleDateString()} — {new Date(row.endDate).toLocaleDateString()}
        </span>
      ),
    },
    { key: 'totalDays', label: 'Days', render: (row) => <span className="font-medium">{row.totalDays}</span> },
    { key: 'reason', label: 'Reason', render: (row) => <span className="text-gray-500 truncate max-w-[200px] block">{row.reason}</span> },
    {
      key: 'actions',
      label: 'Actions',
      render: (row) => (
        <div className="flex gap-1">
          <button
            onClick={(e) => { e.stopPropagation(); approveMutation.mutate({ id: row.id }) }}
            disabled={!employeeId || approveMutation.isPending}
            className="inline-flex items-center gap-1 rounded-lg bg-green-50 px-2.5 py-1.5 text-xs font-medium text-green-700 transition-colors hover:bg-green-100 disabled:opacity-50"
          >
            <CheckCircle fontSize="inherit" /> Approve
          </button>
          <button
            onClick={(e) => { e.stopPropagation(); rejectMutation.mutate({ id: row.id }) }}
            disabled={!employeeId || rejectMutation.isPending}
            className="inline-flex items-center gap-1 rounded-lg bg-red-50 px-2.5 py-1.5 text-xs font-medium text-red-700 transition-colors hover:bg-red-100 disabled:opacity-50"
          >
            <Cancel fontSize="inherit" /> Reject
          </button>
        </div>
      ),
    },
  ]

  if (loadingEmployee || isLoading) return <LoadingSpinner fullPage />

  return (
    <div className="space-y-6 sm:space-y-8">
      <div className="page-header">
        <div>
          <h1 className="page-title">Leave Approvals</h1>
          <p className="page-subtitle">Review and approve pending leave requests from your team</p>
        </div>
      </div>

      {pendingLeaves?.length > 0 ? (
        <DataTable columns={columns} data={pendingLeaves} />
      ) : (
        <EmptyState
          icon={EventBusyOutlined}
          title="No pending approvals"
          description="There are no pending leave requests from your team members."
        />
      )}
    </div>
  )
}
