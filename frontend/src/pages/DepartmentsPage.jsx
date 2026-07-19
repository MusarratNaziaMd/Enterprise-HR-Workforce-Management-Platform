import { useEffect, useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import { Add, Edit, Delete, BusinessOutlined } from '@mui/icons-material'
import { DataTable } from '../components/common/DataTable'
import Modal from '../components/common/Modal'
import EmptyState from '../components/common/EmptyState'
import LoadingSpinner from '../components/common/LoadingSpinner'
import { departmentApi } from '../api/api'

export default function DepartmentsPage() {
  const [showModal, setShowModal] = useState(false)
  const [editDept, setEditDept] = useState(null)
  const queryClient = useQueryClient()

  const { data: departments, isLoading } = useQuery({
    queryKey: ['departments'],
    queryFn: () => departmentApi.getAll().then((r) => r.data.data),
  })

  const deleteMutation = useMutation({
    mutationFn: (id) => departmentApi.delete(id),
    onSuccess: () => { toast.success('Department deleted'); queryClient.invalidateQueries({ queryKey: ['departments'] }) },
    onError: (err) => toast.error(err.response?.data?.message || 'Cannot delete Ã¢â‚¬â€ department has employees'),
  })

  const columns = [
    { key: 'code', label: 'Code' },
    { key: 'name', label: 'Name' },
    { key: 'description', label: 'Description', render: (row) => row.description || 'Ã¢â‚¬â€' },
    { key: 'employeeCount', label: 'Employees', render: (row) => <span className="font-medium">{row.employeeCount}</span> },
    {
      key: 'actions',
      label: '',
      render: (row) => (
        <div className="flex gap-1">
          <button onClick={(e) => { e.stopPropagation(); setEditDept(row); setShowModal(true) }} className="p-1.5 rounded-lg hover:bg-gray-100 text-gray-500">
            <Edit fontSize="small" />
          </button>
          <button onClick={(e) => { e.stopPropagation(); if (confirm('Delete?')) deleteMutation.mutate(row.id) }} className="p-1.5 rounded-lg hover:bg-red-50 text-red-500">
            <Delete fontSize="small" />
          </button>
        </div>
      ),
    },
  ]

  if (isLoading) return <LoadingSpinner fullPage />

  return (
    <div className="space-y-6 sm:space-y-8">
      <div className="page-header">
        <div>
          <h1 className="page-title">Departments</h1>
          <p className="page-subtitle">Manage organizational departments</p>
        </div>
        <button onClick={() => { setEditDept(null); setShowModal(true) }} className="btn-primary flex w-full items-center gap-2 text-sm sm:w-auto">
          <Add fontSize="small" /> Add Department
        </button>
      </div>

      {departments?.length > 0 ? (
        <DataTable columns={columns} data={departments} onRowClick={(row) => { setEditDept(row); setShowModal(true) }} />
      ) : (
        <EmptyState icon={BusinessOutlined} title="No departments" description="Create your first department." />
      )}

      <DeptModal
        isOpen={showModal}
        onClose={() => { setShowModal(false); setEditDept(null) }}
        dept={editDept}
        onSuccess={() => { setShowModal(false); queryClient.invalidateQueries({ queryKey: ['departments'] }) }}
      />
    </div>
  )
}

function DeptModal({ isOpen, onClose, dept, onSuccess }) {
  const [form, setForm] = useState({ name: '', code: '', description: '' })
  const isEdit = !!dept?.id

  useEffect(() => {
    setForm({ name: dept?.name || '', code: dept?.code || '', description: dept?.description || '' })
  }, [dept, isOpen])

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      if (isEdit) {
        await departmentApi.update(dept.id, form)
        toast.success('Department updated')
      } else {
        await departmentApi.create(form)
        toast.success('Department created')
      }
      onSuccess()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Operation failed')
    }
  }

  return (
    <Modal isOpen={isOpen} onClose={onClose} title={isEdit ? 'Edit Department' : 'Add Department'}>
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Name *</label>
          <input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} className="input-field" required />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Code *</label>
          <input value={form.code} onChange={(e) => setForm({ ...form, code: e.target.value })} className="input-field" required maxLength={20} />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Description</label>
          <textarea value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} className="input-field" rows={3} />
        </div>
        <div className="flex flex-col-reverse gap-3 border-t pt-4 sm:flex-row sm:justify-end">
          <button type="button" onClick={onClose} className="btn-secondary text-sm">Cancel</button>
          <button type="submit" className="btn-primary text-sm">{isEdit ? 'Update' : 'Create'}</button>
        </div>
      </form>
    </Modal>
  )
}
