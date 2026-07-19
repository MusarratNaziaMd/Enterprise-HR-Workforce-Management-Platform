import { useState, useEffect } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import { Add, Edit, Delete, FilterList, Search } from '@mui/icons-material'
import { DataTable, Pagination } from '../components/common/DataTable'
import { Badge } from '../components/common/Badge'
import Modal from '../components/common/Modal'
import LoadingSpinner from '../components/common/LoadingSpinner'
import { employeeApi, departmentApi } from '../api/api'

export default function EmployeesPage() {
  const [page, setPage] = useState(0)
  const [search, setSearch] = useState('')
  const [showModal, setShowModal] = useState(false)
  const [editEmployee, setEditEmployee] = useState(null)
  const [createdCredentials, setCreatedCredentials] = useState(null)
  const queryClient = useQueryClient()

  const { data, isLoading } = useQuery({
    queryKey: ['employees', { page, search }],
    queryFn: () =>
      search
        ? employeeApi.search({ keyword: search, page, size: 10 })
        : employeeApi.getAll({ page, size: 10 }),
    select: (res) => res.data.data,
  })

  const { data: departments } = useQuery({
    queryKey: ['departments'],
    queryFn: () => departmentApi.getAll().then((r) => r.data.data),
  })

  const deleteMutation = useMutation({
    mutationFn: (id) => employeeApi.delete(id),
    onSuccess: () => {
      toast.success('Employee deleted successfully')
      queryClient.invalidateQueries({ queryKey: ['employees'] })
    },
    onError: (err) => toast.error(err.response?.data?.message || 'Delete failed'),
  })

  const columns = [
    { key: 'employeeCode', label: 'Code' },
    {
      key: 'fullName',
      label: 'Employee Name',
      render: (row) => (
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 bg-slate-200 text-slate-600 rounded-full flex items-center justify-center font-bold text-sm">
            {row.firstName?.[0]}{row.lastName?.[0]}
          </div>
          <span className="font-semibold text-slate-900">{row.fullName}</span>
        </div>
      ),
    },
    { key: 'designation', label: 'Designation' },
    { key: 'departmentName', label: 'Department' },
    { key: 'status', label: 'Status', render: (row) => <Badge status={row.status} /> },
    {
      key: 'actions',
      label: 'Actions',
      render: (row) => (
        <div className="flex items-center gap-2">
          <button onClick={() => { setEditEmployee(row); setShowModal(true) }} className="text-slate-400 hover:text-blue-600 transition">
            <Edit fontSize="small" />
          </button>
          <button onClick={() => { if (confirm('Delete this employee?')) deleteMutation.mutate(row.id) }} className="text-slate-400 hover:text-rose-600 transition">
            <Delete fontSize="small" />
          </button>
        </div>
      ),
    },
  ]

  if (isLoading) return <LoadingSpinner fullPage />

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-slate-900">Employees</h1>
        <button
          onClick={() => { setEditEmployee(null); setShowModal(true) }}
          className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg flex items-center gap-2 text-sm font-medium transition"
        >
          <Add fontSize="small" /> Add Employee
        </button>
      </div>

      <div className="bg-white p-4 rounded-2xl shadow-sm border border-slate-100 flex items-center gap-4">
        <div className="relative flex-1">
            <Search className="absolute left-3 top-2.5 text-slate-400" fontSize="small" />
            <input
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                placeholder="Search by name, email, or code..."
                className="w-full pl-10 pr-4 py-2 bg-slate-50 rounded-lg border-none focus:ring-2 focus:ring-blue-500 outline-none"
            />
        </div>
        <button className="text-slate-500 hover:text-slate-900 flex items-center gap-2 text-sm font-medium px-4 py-2">
            <FilterList fontSize="small" /> Filters
        </button>
      </div>

      <div className="bg-white rounded-2xl shadow-sm border border-slate-100 overflow-hidden">
        <DataTable columns={columns} data={data?.content || []} />
        <div className="p-4 border-t border-slate-100">
            <Pagination page={data?.pageNumber} totalPages={data?.totalPages} onPageChange={setPage} />
        </div>
      </div>

      <EmployeeModal
        isOpen={showModal}
        onClose={() => { setShowModal(false); setEditEmployee(null) }}
        employee={editEmployee}
        departments={departments || []}
        onSuccess={() => { setShowModal(false); queryClient.invalidateQueries({ queryKey: ['employees'] }) }}
        setCreatedCredentials={setCreatedCredentials}
      />

      <Modal
        isOpen={!!createdCredentials}
        onClose={() => setCreatedCredentials(null)}
        title="Employee Created — Login Credentials"
        maxWidth="max-w-md"
      >
        <div className="space-y-4">
          <p className="text-sm text-gray-600">
            A user account has been created for <strong>{createdCredentials?.name}</strong> ({createdCredentials?.employeeCode}).
            Share these credentials securely with the employee.
          </p>
          <div className="bg-gray-50 rounded-lg p-4 space-y-3">
            <div>
              <span className="text-xs font-medium text-gray-500 uppercase tracking-wide">Username</span>
              <p className="font-mono text-sm text-gray-900 mt-1">{createdCredentials?.username}</p>
            </div>
            <div>
              <span className="text-xs font-medium text-gray-500 uppercase tracking-wide">Temporary Password</span>
              <p className="font-mono text-sm text-gray-900 mt-1">{createdCredentials?.password}</p>
            </div>
          </div>
          <div className="flex justify-end">
            <button onClick={() => setCreatedCredentials(null)} className="btn-primary text-sm">Got it</button>
          </div>
        </div>
      </Modal>
    </div>
  )
}

function EmployeeModal({ isOpen, onClose, employee, departments, onSuccess, setCreatedCredentials }) {
  const [form, setForm] = useState({})
  const isEdit = !!employee?.id

  useEffect(() => {
    if (employee?.id) {
      setForm({
        firstName: employee.firstName || '',
        lastName: employee.lastName || '',
        designation: employee.designation || '',
        departmentId: employee.departmentId || '',
        employmentType: employee.employmentType || 'FULL_TIME',
      })
    } else {
      setForm({ employeeCode: '', firstName: '', lastName: '', email: '', designation: '', departmentId: '', employmentType: 'FULL_TIME', dateOfJoining: '' })
    }
  }, [employee, isOpen])

  const set = (field) => (e) => setForm({ ...form, [field]: e.target.value })

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      if (isEdit) {
        await employeeApi.update(employee.id, {
          firstName: form.firstName,
          lastName: form.lastName,
          designation: form.designation,
          departmentId: form.departmentId ? Number(form.departmentId) : null,
          employmentType: form.employmentType || 'FULL_TIME',
        })
        toast.success('Employee updated')
      } else {
        const res = await employeeApi.create({
          employeeCode: form.employeeCode,
          firstName: form.firstName,
          lastName: form.lastName,
          email: form.email,
          designation: form.designation,
          departmentId: Number(form.departmentId),
          employmentType: form.employmentType || 'FULL_TIME',
          dateOfJoining: form.dateOfJoining || new Date().toISOString().split('T')[0],
        })
        const data = res.data.data
        if (data.generatedUsername) {
          setCreatedCredentials({
            username: data.generatedUsername,
            password: data.generatedPassword,
            employeeCode: data.employeeCode,
            name: `${data.firstName} ${data.lastName}`,
          })
        }
        toast.success('Employee created')
      }
      onSuccess()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Operation failed')
    }
  }

  return (
    <Modal isOpen={isOpen} onClose={onClose} title={isEdit ? 'Edit Employee' : 'Add Employee'} maxWidth="max-w-2xl">
      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          {!isEdit && (
            <>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Employee Code *</label>
                <input value={form.employeeCode || ''} onChange={set('employeeCode')} className="input-field" required />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Email *</label>
                <input type="email" value={form.email || ''} onChange={set('email')} className="input-field" required />
              </div>
            </>
          )}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">First Name *</label>
            <input value={form.firstName || ''} onChange={set('firstName')} className="input-field" required />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Last Name *</label>
            <input value={form.lastName || ''} onChange={set('lastName')} className="input-field" required />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Designation *</label>
            <input value={form.designation || ''} onChange={set('designation')} className="input-field" required />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Department *</label>
            <select value={form.departmentId || ''} onChange={set('departmentId')} className="input-field" required>
              <option value="">Select department</option>
              {departments.map((d) => (
                <option key={d.id} value={d.id}>{d.name}</option>
              ))}
            </select>
          </div>
          {!isEdit && (
            <>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Employment Type</label>
                <select value={form.employmentType || 'FULL_TIME'} onChange={set('employmentType')} className="input-field">
                  <option value="FULL_TIME">Full Time</option>
                  <option value="PART_TIME">Part Time</option>
                  <option value="CONTRACT">Contract</option>
                  <option value="INTERN">Intern</option>
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Date of Joining *</label>
                <input type="date" value={form.dateOfJoining || ''} onChange={set('dateOfJoining')} className="input-field" required />
              </div>
            </>
          )}
        </div>
        <div className="flex flex-col-reverse gap-3 border-t pt-4 sm:flex-row sm:justify-end">
          <button type="button" onClick={onClose} className="btn-secondary text-sm">Cancel</button>
          <button type="submit" className="btn-primary text-sm">{isEdit ? 'Update' : 'Create'}</button>
        </div>
      </form>
    </Modal>
  )
}
