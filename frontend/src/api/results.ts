import type { ContestResult } from '../types/result'
import { apiRequest } from './client'

export function getContestResult(
  electionId: string,
  contestId: string,
  signal?: AbortSignal,
) {
  const encodedElectionId = encodeURIComponent(electionId)
  const encodedContestId = encodeURIComponent(contestId)

  return apiRequest<ContestResult>(
    `/api/v1/elections/${encodedElectionId}/contests/${encodedContestId}/results`,
    { signal },
  )
}
