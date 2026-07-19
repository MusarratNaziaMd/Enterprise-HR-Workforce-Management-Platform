export function Badge({ status }) {
  const map = {
    ACTIVE: 'badge-success',
    PRESENT: 'badge-success',
    APPROVED: 'badge-success',
    PAID: 'badge-success',
    PROBATION: 'badge-warning',
    ON_LEAVE: 'badge-warning',
    PENDING: 'badge-warning',
    PROCESSING: 'badge-warning',
    ABSENT: 'badge-danger',
    TERMINATED: 'badge-danger',
    REJECTED: 'badge-danger',
    SUSPENDED: 'badge-danger',
    FAILED: 'badge-danger',
    CANCELLED: 'badge-danger',
    RESIGNED: 'badge-info',
    HALF_DAY: 'badge-info',
    WORK_FROM_HOME: 'badge-info',
    HOLIDAY: 'badge-info',
    WEEK_OFF: 'badge-info',
  }

  return (
    <span className={map[status] || 'badge bg-gray-100 text-gray-700'}>
      {status?.replace(/_/g, ' ')}
    </span>
  )
}
