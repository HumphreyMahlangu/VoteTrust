export type AccountRole = 'VOTER' | 'ADMIN'

export interface AuthSession {
  accessToken: string
  tokenType: string
  expiresAt: string
  userId: string
  email: string
  role: AccountRole
}

export interface LoginCredentials {
  email: string
  password: string
}

export interface RegistrationDetails {
  email: string
  password: string
}
