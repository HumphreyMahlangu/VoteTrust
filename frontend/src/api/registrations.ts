import type { ElectionRegistration } from '../types/registration'
import { apiRequest } from './client'

export function getMyRegistrations(
  authToken: string,
  signal?: AbortSignal,
) {
  return apiRequest<ElectionRegistration[]>('/api/v1/me/registrations', {
    authToken,
    signal,
  })
}
