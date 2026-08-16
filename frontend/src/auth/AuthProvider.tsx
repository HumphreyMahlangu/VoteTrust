import { useEffect, useState, type PropsWithChildren } from 'react'
import { loginAccount, registerAccount } from '../api/auth'
import type {
  AuthSession,
  LoginCredentials,
  RegistrationDetails,
} from '../types/auth'
import { AuthContext } from './context'

function AuthProvider({ children }: PropsWithChildren) {
  const [session, setSession] = useState<AuthSession | null>(null)

  useEffect(() => {
    if (!session) {
      return
    }

    const expiresIn = new Date(session.expiresAt).getTime() - Date.now()

    if (expiresIn <= 0) {
      setSession(null)
      return
    }

    const expirationTimer = window.setTimeout(() => {
      setSession(null)
    }, expiresIn)

    return () => window.clearTimeout(expirationTimer)
  }, [session])

  async function login(credentials: LoginCredentials) {
    const authenticatedSession = await loginAccount(credentials)
    setSession(authenticatedSession)
    return authenticatedSession
  }

  async function register(details: RegistrationDetails) {
    const authenticatedSession = await registerAccount(details)
    setSession(authenticatedSession)
    return authenticatedSession
  }

  function logout() {
    setSession(null)
  }

  return (
    <AuthContext.Provider value={{ session, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export default AuthProvider
