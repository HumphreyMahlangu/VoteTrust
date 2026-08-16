import { createContext } from 'react'
import type {
  AuthSession,
  LoginCredentials,
  RegistrationDetails,
} from '../types/auth'

export interface AuthContextValue {
  session: AuthSession | null
  login: (credentials: LoginCredentials) => Promise<AuthSession>
  register: (details: RegistrationDetails) => Promise<AuthSession>
  logout: () => void
}

export const AuthContext = createContext<AuthContextValue | undefined>(undefined)
