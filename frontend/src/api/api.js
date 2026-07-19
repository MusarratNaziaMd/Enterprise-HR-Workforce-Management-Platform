import api from './axios'

export const authApi = {
  login: (credentials) => api.post('/auth/login', credentials),
  register: (data) => api.post('/auth/register', data),
  refreshToken: (refreshToken) => api.post('/auth/refresh', { refreshToken }),
}

export const employeeApi = {
  getAll: (params) => api.get('/employees', { params }),
  search: (params) => api.get('/employees/search', { params }),
  getById: (id) => api.get(`/employees/${id}`),
  getByCode: (code) => api.get(`/employees/code/${code}`),
  getByUserId: (userId) => api.get(`/employees/user/${userId}`),
  create: (data) => api.post('/employees', data),
  update: (id, data) => api.put(`/employees/${id}`, data),
  delete: (id) => api.delete(`/employees/${id}`),
}

export const departmentApi = {
  getAll: () => api.get('/departments'),
  getById: (id) => api.get(`/departments/${id}`),
  create: (data) => api.post('/departments', data),
  update: (id, data) => api.put(`/departments/${id}`, data),
  delete: (id) => api.delete(`/departments/${id}`),
}

export const attendanceApi = {
  clockIn: (data) => api.post('/attendance/clock-in', data),
  clockOut: (data) => api.post('/attendance/clock-out', data),
  mark: (data) => api.post('/attendance', data),
  getEmployeeAttendance: (empId, params) =>
    api.get(`/attendance/employee/${empId}`, { params }),
  getByDateRange: (empId, startDate, endDate) =>
    api.get(`/attendance/employee/${empId}/range`, { params: { startDate, endDate } }),
}

export const leaveTypeApi = {
  getAll: () => api.get('/leave-types'),
}

export const leaveApi = {
  apply: (data) => api.post('/leaves', data),
  approve: (id, data) => api.put(`/leaves/${id}/approve`, data),
  reject: (id, data) => api.put(`/leaves/${id}/reject`, data),
  cancel: (id, employeeId) => api.put(`/leaves/${id}/cancel`, null, { params: { employeeId } }),
  getEmployeeLeaves: (empId, params) =>
    api.get(`/leaves/employee/${empId}`, { params }),
  getPendingByManager: (managerId) =>
    api.get(`/leaves/manager/${managerId}/pending`),
  getAllPending: () => api.get('/leaves/pending'),
  getBalance: (empId, year) =>
    api.get(`/leaves/balance/${empId}`, { params: { year } }),
}
