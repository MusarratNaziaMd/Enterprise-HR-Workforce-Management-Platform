import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import { Add, EventOutlined, CheckCircle, Cancel, PendingOutlined } from '@mui/icons-material'
import { DataTable, Pagination } from '../components/common/DataTable'
import { Badge } from '../components/common/Badge'
import Modal from '../components/common/Modal'
import LoadingSpinner from '../components/common/LoadingSpinner'
import EmptyState from '../components/common/EmptyState'
import { leaveApi, leaveTypeApi } from '../api/api'
import { useCurrentUserEmployee } from '../hooks/useCurrentUserEmployee'
import { useRole } from '../hooks/useRole'

export default function LeavesPage() {
  const [page, setPage] = useState(0)
  const [showApply, setShowApply] = useState(false)
  const queryClient = useQueryClient()
  const { employeeId, employee, isLoading: loadingEmployee } = useCurrentUserEmployee()
  const { isAdmin, isHrManager } = useRole()

  const { data, isLoading } = useQuery({
    queryKey: ['leaves', employeeId, page],
    queryFn: () => leaveApi.getEmployeeLeaves(employeeId, { page, size: 10 }).then((r) => r.data.data),
    enabled: !!employeeId,
  })

  const { data: balance } = useQuery({
    queryKey: ['leaveBalance', employeeId],
    queryFn: () => leaveApi.getBalance(employeeId, new Date().getFullYear()).then((r) => r.data.data),
    enabled: !!employeeId,
  })

  const { data: leaveTypes } = useQuery({
    queryKey: ['leaveTypes'],
    queryFn: () => leaveTypeApi.getAll().then((r) => r.data.data),
  })

  const { data: pendingLeaves } = useQuery({
    queryKey: ['pendingLeaves'],
    queryFn: () => leaveApi.getAllPending().then((r) => r.data.data),
    enabled: isHrManager,
  })

  const approveMutation = useMutation({
    mutationFn: ({ id }) => leaveApi.approve(id, { approvedById: employeeId }),
    onSuccess: () => { toast.success('Leave approved'); queryClient.invalidateQueries({ queryKey: ['pendingLeaves'] }); queryClient.invalidateQueries({ queryKey: ['leaves'] }); queryClient.invalidateQueries({ queryKey: ['leaveBalance'] }) },
    onError: (err) => toast.error(err.response?.data?.message || 'Failed to approve'),
  })

  const rejectMutation = useMutation({
    mutationFn: ({ id }) => leaveApi.reject(id, { approvedById: employeeId, rejectionReason: 'Rejected by manager' }),
    onSuccess: () => { toast.success('Leave rejected'); queryClient.invalidateQueries({ queryKey: ['pendingLeaves'] }); queryClient.invalidateQueries({ queryKey: ['leaves'] }); queryClient.invalidateQueries({ queryKey: ['leaveBalance'] }) },
    onError: (err) => toast.error(err.response?.data?.message || 'Failed to reject'),
  })

  const columns = [
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
    { key: 'status', label: 'Status', render: (row) => <Badge status={row.status} /> },
    { key: 'reason', label: 'Reason', render: (row) => <span className="text-gray-500 truncate max-w-[200px] block">{row.reason}</span> },
  ]

  const pendingColumns = [
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
      render: (row) => <span className="text-sm font-medium">{row.leaveTypeName}</span>,
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
    { key: 'reason', label: 'Reason', render: (row) => <span className="text-gray-500 truncate max-w-[150px] block">{row.reason}</span> },
    {
      key: 'actions',
      label: 'Actions',
      render: (row) => (
        <div className="flex gap-1">
          <button
            onClick={(e) => { e.stopPropagation(); approveMutation.mutate({ id: row.id }) }}
            disabled={approveMutation.isPending}
            className="inline-flex items-center gap-1 rounded-lg bg-green-50 px-2.5 py-1.5 text-xs font-medium text-green-700 transition-colors hover:bg-green-100"
          >
            <CheckCircle fontSize="inherit" /> Approve
          </button>
          <button
            onClick={(e) => { e.stopPropagation(); rejectMutation.mutate({ id: row.id }) }}
            disabled={rejectMutation.isPending}
            className="inline-flex items-center gap-1 rounded-lg bg-red-50 px-2.5 py-1.5 text-xs font-medium text-red-700 transition-colors hover:bg-red-100"
          >
            <Cancel fontSize="inherit" /> Reject
          </button>
        </div>
      ),
    },
  ]

  if (loadingEmployee || isLoading) return <LoadingSpinner fullPage />

  if (!employeeId) {
    return (
      <div className="space-y-6 sm:space-y-8">
        <div className="page-header">
          <div>
            <h1 className="page-title">My Leaves</h1>
            <p className="page-subtitle">Apply and manage your leave requests</p>
          </div>
        </div>
        <EmptyState
          icon={EventOutlined}
          title="No employee profile"
          description="Admin accounts don't have employee profiles. Leave management requires an employee account."
        />
      </div>
    )
  }

  return (
    <div className="space-y-6 sm:space-y-8">
      <div className="page-header">
        <div>
          <h1 className="page-title">My Leaves</h1>
          <p className="page-subtitle">Apply and manage your leave requests</p>
        </div>
        <button onClick={() => setShowApply(true)} disabled={!employeeId} className="btn-primary flex w-full items-center gap-2 text-sm sm:w-auto">
          <Add fontSize="small" /> Apply Leave
        </button>
      </div>

      {isHrManager && pendingLeaves?.length > 0 && (
        <div className="space-y-3">
          <div className="flex items-center gap-2">
            <PendingOutlined className="text-amber-500" fontSize="small" />
            <h2 className="text-lg font-semibold text-gray-900">Pending Approvals</h2>
            <span className="badge bg-amber-100 text-amber-700">{pendingLeaves.length}</span>
          </div>
          <DataTable columns={pendingColumns} data={pendingLeaves} />
        </div>
      )}

      {balance?.length > 0 && (
        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-3">
          {balance.map((b) => (
            <div key={b.leaveTypeId} className="card p-4 text-center">
              <p className="text-xs font-medium text-gray-500 uppercase tracking-wide">{b.leaveTypeCode}</p>
              <p className="text-2xl font-bold text-gray-900 mt-1">{b.remaining}</p>
              <p className="text-xs text-gray-400">of {b.totalEntitled} remaining</p>
            </div>
          ))}
        </div>
      )}

      {data?.content?.length > 0 ? (
        <>
          <DataTable columns={columns} data={data.content} />
          <Pagination page={data.pageNumber} totalPages={data.totalPages} onPageChange={setPage} />
        </>
      ) : (
        <EmptyState icon={EventOutlined} title="No leave records" description="You haven't applied for any leaves yet." />
      )}

      <ApplyLeaveModal
        isOpen={showApply}
        onClose={() => setShowApply(false)}
        employeeId={employeeId}
        leaveTypes={leaveTypes || []}
        onSuccess={() => { setShowApply(false); queryClient.invalidateQueries({ queryKey: ['leaves'] }); queryClient.invalidateQueries({ queryKey: ['leaveBalance'] }) }}
      />
    </div>
  )
}

function ApplyLeaveModal({ isOpen, onClose, employeeId, leaveTypes, onSuccess }) {
  const [form, setForm] = useState({ leaveTypeId: '', startDate: '', endDate: '', reason: '' })
  const set = (field) => (e) => setForm({ ...form, [field]: e.target.value })

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      await leaveApi.apply({
        employeeId,
        leaveTypeId: Number(form.leaveTypeId),
        startDate: form.startDate,
        endDate: form.endDate,
        reason: form.reason,
      })
      toast.success('Leave applied')
      onSuccess()
      setForm({ leaveTypeId: '', startDate: '', endDate: '', reason: '' })
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to apply leave')
    }
  }

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Apply for Leave">
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Leave Type *</label>
          <select value={form.leaveTypeId} onChange={set('leaveTypeId')} className="input-field" required>
            <option value="">Select type</option>
            {leaveTypes.map((lt) => (
              <option key={lt.id} value={lt.id}>{lt.name}</option>
            ))}
          </select>
        </div>
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Start Date *</label>
            <input type="date" value={form.startDate} onChange={set('startDate')} className="input-field" required />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">End Date *</label>
            <input type="date" value={form.endDate} onChange={set('endDate')} className="input-field" required />
          </div>
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Reason *</label>
          <textarea value={form.reason} onChange={set('reason')} className="input-field" rows={3} required minLength={10} placeholder="Describe the reason for your leave..." />
        </div>
        <div className="flex flex-col-reverse gap-3 border-t pt-4 sm:flex-row sm:justify-end">
          <button type="button" onClick={onClose} className="btn-secondary text-sm">Cancel</button>
          <button type="submit" className="btn-primary text-sm">Submit Application</button>
        </div>
      </form>
    </Modal>
  )
}
