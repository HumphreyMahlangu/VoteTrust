export type ElectionType = 'NATIONAL' | 'PROVINCIAL' | 'MUNICIPAL'

export type ElectionStatus =
  | 'DRAFT'
  | 'REGISTRATION_OPEN'
  | 'REGISTRATION_CLOSED'
  | 'VOTING_OPEN'
  | 'COMPLETED'
  | 'CANCELLED'

export interface Election {
  id: string
  name: string
  type: ElectionType
  status: ElectionStatus
  registrationStartAt: string
  registrationEndAt: string
  votingStartAt: string
  votingEndAt: string
}

export interface CreateElectionRequest {
  name: string
  type: ElectionType
  registrationStartAt: string
  registrationEndAt: string
  votingStartAt: string
  votingEndAt: string
}
