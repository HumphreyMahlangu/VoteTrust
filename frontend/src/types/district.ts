export interface VotingDistrict {
  id: string
  code: string
  name: string
  province: string
  municipality: string
  wardNumber: number
}

export interface CreateVotingDistrictRequest {
  code: string
  name: string
  province: string
  municipality: string
  wardNumber: number
}
