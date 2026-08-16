export interface ContestAudit {
  electionId: string
  contestId: string
  chainValid: boolean
  ledgerEntryCount: number
  genesisHash: string
  computedHeadHash: string
  storedHeadHash: string
  storedNextLedgerIndex: number | null
  verifiedAt: string
  violations: string[]
}

export interface ContestLedgerEntry {
  ledgerIndex: number
  contestOptionId: string
  previousHash: string
  currentHash: string
  nonce: string
  recordedDate: string
}
