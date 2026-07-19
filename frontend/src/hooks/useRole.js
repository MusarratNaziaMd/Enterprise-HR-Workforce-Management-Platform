import { useMemo } from 'react'
import { useAuth } from './useAuth'

export function useRole() {
  const { user, hasAuthority } = useAuth()

  const isAdmin = useMemo(() => {
    return user?.portal === 'ADMIN'
  }, [user])

  const isHrManager = useMemo(() => {
    return hasAuthority('LEAVE_APPROVE')
  }, [hasAuthority])

  return { isAdmin, isHrManager, hasAuthority, user }
}
