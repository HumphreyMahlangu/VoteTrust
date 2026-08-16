export type ContestType =
  | 'NATIONAL'
  | 'PROVINCIAL'
  | 'MUNICIPAL_WARD'
  | 'MUNICIPAL_PR'

export type ContestStatus = 'DRAFT' | 'OPEN' | 'CLOSED'

export type ContestOptionType =
  | 'PARTY'
  | 'INDEPENDENT_CANDIDATE'
  | 'BLANK_BALLOT'
  | 'SPOILT_BALLOT'

export interface ContestOption {
  id: string
  name: string
  optionType: ContestOptionType
  displayOrder: number
}

export interface Contest {
  id: string
  electionId: string
  name: string
  type: ContestType
  status: ContestStatus
  displayOrder: number
  scopeProvince: string | null
  scopeMunicipality: string | null
  scopeWardNumber: number | null
  options: ContestOption[]
}
