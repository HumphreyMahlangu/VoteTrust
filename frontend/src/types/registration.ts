import type { ElectionType } from './election'

export type RegistrationStatus = 'ACTIVE' | 'CANCELLED'

export interface ElectionRegistration {
  id: string
  electionId: string
  electionName: string
  electionType: ElectionType
  status: RegistrationStatus
  registeredAt: string
  votingDistrictId: string
  votingDistrictCode: string
  votingDistrictName: string
  province: string
}
