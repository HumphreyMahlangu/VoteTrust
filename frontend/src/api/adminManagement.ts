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
