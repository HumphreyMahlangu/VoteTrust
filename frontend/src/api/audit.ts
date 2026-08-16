import type { ContestAudit, ContestLedgerEntry } from '../types/audit'
import { apiRequest } from './client'

function getContestPath(electionId: string, contestId: string) {
  const encodedElectionId = encodeURIComponent(electionId)
  const encodedContestId = encodeURIComponent(contestId)
  return `/api/v1/elections/${encodedElectionId}/contests/${encodedContestId}`
}

export function getContestAudit(
  electionId: string,
  contestId: string,
  signal?: AbortSignal,
) {
  return apiRequest<ContestAudit>(
    `${getContestPath(electionId, contestId)}/audit`,
    { signal },
  )
}

export function getContestLedger(
  electionId: string,
  contestId: string,
  signal?: AbortSignal,
) {
  return apiRequest<ContestLedgerEntry[]>(
    `${getContestPath(electionId, contestId)}/ledger`,
    { signal },
  )
}
