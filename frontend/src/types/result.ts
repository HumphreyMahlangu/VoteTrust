import type { ContestOptionType, ContestType } from './contest'

export interface ContestOptionResult {
  contestOptionId: string
  name: string
  optionType: ContestOptionType
  displayOrder: number
  voteCount: number
  percentageOfValidVotes: number
  leading: boolean
}

export interface ContestResult {
  electionId: string
  contestId: string
  contestName: string
  contestType: ContestType
  registeredVoterCount: number
  ballotsCast: number
  validVotes: number
  blankBallots: number
  spoiltBallots: number
  turnoutPercentage: number
  ledgerHeadHash: string
  generatedAt: string
  options: ContestOptionResult[]
}
