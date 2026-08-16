import type { Election } from '../types/election'
import { apiRequest } from './client'

const ELECTIONS_PATH = '/api/v1/elections'

export function getElections(signal?: AbortSignal) {
  return apiRequest<Election[]>(ELECTIONS_PATH, { signal })
}

export function getElection(electionId: string, signal?: AbortSignal) {
  const encodedElectionId = encodeURIComponent(electionId)
  return apiRequest<Election>(`${ELECTIONS_PATH}/${encodedElectionId}`, {
    signal,
  })
}
