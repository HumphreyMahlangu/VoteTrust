import type { VotingDistrict } from '../types/district'
import { apiRequest } from './client'

export function getVotingDistricts(signal?: AbortSignal) {
  return apiRequest<VotingDistrict[]>('/api/v1/voting-districts', { signal })
}
