import type {
  BallotCastRequest,
  BallotCastResponse,
  VotingCredential,
} from '../types/voting'
import { apiRequest } from './client'

export function issueVotingCredential(
  electionId: string,
  contestId: string,
  authToken: string,
) {
  const encodedElectionId = encodeURIComponent(electionId)
  const encodedContestId = encodeURIComponent(contestId)

  return apiRequest<VotingCredential>(
    `/api/v1/elections/${encodedElectionId}/contests/${encodedContestId}/credentials`,
    {
      method: 'POST',
      authToken,
    },
  )
}

export function castBallot(request: BallotCastRequest) {
  return apiRequest<BallotCastResponse>('/api/v1/ballots', {
    method: 'POST',
    body: request,
  })
}
