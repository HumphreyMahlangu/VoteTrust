import type { SecurityAuditEvent } from '../types/securityAudit'
import { apiRequest } from './client'

const SECURITY_AUDIT_EVENTS_PATH = '/api/v1/admin/security-audit-events'

export function getSecurityAuditEvents(
  authToken: string,
  limit: number,
  signal?: AbortSignal,
) {
  return apiRequest<SecurityAuditEvent[]>(
    `${SECURITY_AUDIT_EVENTS_PATH}?limit=${limit}`,
    {
      authToken,
      signal,
    },
  )
}
