import type { ElectionStatus } from './election'

export type ElectionLifecycleTrigger = 'AUTOMATIC' | 'ADMINISTRATOR'

export type ElectionLifecycleOutcome = 'SUCCESS' | 'FAILURE'

export interface ElectionLifecycleEvent {
  id: string
  electionId: string
  previousStatus: ElectionStatus
  newStatus: ElectionStatus | null
  trigger: ElectionLifecycleTrigger
  outcome: ElectionLifecycleOutcome
  actorUserId: string | null
  actorEmail: string | null
  detail: string
  occurredAt: string
}
