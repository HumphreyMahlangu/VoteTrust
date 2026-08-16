import type {
  Contest,
  ContestOption,
  CreateContestOptionRequest,
  CreateContestRequest,
} from '../types/contest'
import type {
  CreateVotingDistrictRequest,
  VotingDistrict,
} from '../types/district'
import type { CreateElectionRequest, Election } from '../types/election'
import { apiRequest } from './client'

const ADMIN_PATH = '/api/v1/admin'

export function createVotingDistrict(
  request: CreateVotingDistrictRequest,
  authToken: string,
) {
  return apiRequest<VotingDistrict>(`${ADMIN_PATH}/voting-districts`, {
    method: 'POST',
    authToken,
    body: request,
  })
}

export function createElection(
  request: CreateElectionRequest,
  authToken: string,
) {
  return apiRequest<Election>(`${ADMIN_PATH}/elections`, {
    method: 'POST',
    authToken,
    body: request,
  })
}

export function createContest(
  electionId: string,
  request: CreateContestRequest,
  authToken: string,
) {
  const encodedElectionId = encodeURIComponent(electionId)

  return apiRequest<Contest>(
    `${ADMIN_PATH}/elections/${encodedElectionId}/contests`,
    {
      method: 'POST',
      authToken,
      body: request,
    },
  )
}

export function createContestOption(
  electionId: string,
  contestId: string,
  request: CreateContestOptionRequest,
  authToken: string,
) {
  const encodedElectionId = encodeURIComponent(electionId)
  const encodedContestId = encodeURIComponent(contestId)

  return apiRequest<ContestOption>(
    `${ADMIN_PATH}/elections/${encodedElectionId}/contests/${encodedContestId}/options`,
    {
      method: 'POST',
      authToken,
      body: request,
    },
  )
}
