import { createContext, useState, useEffect, useCallback } from 'react'
import { authApi } from '../api/api'

const AuthContext = createContext(null)

function storage(remember) {
  return remember ? localStorage : sessionStorage
}

function readSession() {
  const ls = localStorage.getItem('user')
  const ss = sessionStorage.getItem('user')
  const lsToken = localStorage.getItem('accessToken')
  const ssToken = sessionStorage.getItem('accessToken')
  if (ls && lsToken) return { user: JSON.parse(ls), token: lsToken, refresh: localStorage.getItem('refreshToken'), remember: true }
  if (ss && ssToken) return { user: JSON.parse(ss), token: ssToken, refresh: sessionStorage.getItem('refreshToken'), remember: false }
  return null
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const session = readSession()
    if (session) setUser(session.user)
    setLoading(false)
  }, [])

  const login = useCallback(async (username, password, portal, remember = true) => {
    const { data: response } = await authApi.login({ username, password, portal })
    const authData = response.data
    const s = storage(remember)
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('user')
    sessionStorage.removeItem('accessToken')
    sessionStorage.removeItem('refreshToken')
    sessionStorage.removeItem('user')
    s.setItem('accessToken', authData.accessToken)
    s.setItem('refreshToken', authData.refreshToken)
    const userData = {
      id: authData.userId,
      username: authData.username,
      email: authData.email,
      portal: authData.portal,
      authorities: authData.authorities,
    }
    s.setItem('user', JSON.stringify(userData))
    setUser(userData)
    return userData
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('user')
    sessionStorage.removeItem('accessToken')
    sessionStorage.removeItem('refreshToken')
    sessionStorage.removeItem('user')
    setUser(null)
  }, [])

  const hasAuthority = useCallback(
    (authority) => user?.authorities?.includes(authority) ?? false,
    [user]
  )

  return (
    <AuthContext.Provider value={{ user, loading, login, logout, hasAuthority }}>
      {children}
    </AuthContext.Provider>
  )
}

export { AuthContext }
