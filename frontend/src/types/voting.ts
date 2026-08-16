export interface VotingCredential {
  electionId: string
  contestId: string
  votingCredential: string
  expiresAt: string
}

export interface BallotCastRequest {
  contestId: string
  contestOptionId: string
  votingCredential: string
}

export interface BallotCastResponse {
  contestId: string
  accepted: boolean
  message: string
}
