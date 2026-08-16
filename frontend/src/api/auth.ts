import type {
  AuthSession,
  LoginCredentials,
  RegistrationDetails,
} from '../types/auth'
import { apiRequest } from './client'

export function loginAccount(credentials: LoginCredentials) {
  return apiRequest<AuthSession>('/api/v1/auth/login', {
    method: 'POST',
    body: credentials,
  })
}

export function registerAccount(details: RegistrationDetails) {
  return apiRequest<AuthSession>('/api/v1/auth/register', {
    method: 'POST',
    body: details,
  })
}
