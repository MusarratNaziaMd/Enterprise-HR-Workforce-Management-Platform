import { useQuery } from '@tanstack/react-query'
import { useAuth } from './useAuth'
import { employeeApi } from '../api/api'

export function useCurrentUserEmployee() {
  const { user } = useAuth()

  const { data, isLoading, error } = useQuery({
    queryKey: ['currentEmployee', user?.id],
    queryFn: () => employeeApi.getByUserId(user.id).then((r) => r.data.data),
    enabled: !!user?.id,
  })

  return { employee: data, isLoading, error, employeeId: data?.id }
}
