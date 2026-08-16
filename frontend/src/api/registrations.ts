import type {
  ElectionRegistration,
  ElectionRegistrationRequest,
} from '../types/registration'
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

export function registerForElection(
  electionId: string,
  request: ElectionRegistrationRequest,
  authToken: string,
) {
  const encodedElectionId = encodeURIComponent(electionId)

  return apiRequest<ElectionRegistration>(
    `/api/v1/elections/${encodedElectionId}/registrations`,
    {
      method: 'POST',
      authToken,
      body: request,
    },
  )
}
