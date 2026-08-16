import { useEffect, useState } from 'react'
import { Link } from 'react-router'
import { getVotingDistricts } from '../api/districts'
import { getElections } from '../api/elections'
import type { VotingDistrict } from '../types/district'
import type { Election, ElectionStatus } from '../types/election'
import { formatEnumLabel, formatNumber } from '../utils/formatters'
import { getErrorMessage } from '../utils/getErrorMessage'

interface AdminDashboardData {
  elections: Election[]
  districts: VotingDistrict[]
}

const ELECTION_STATUSES: ElectionStatus[] = [
  'DRAFT',
  'REGISTRATION_OPEN',
  'REGISTRATION_CLOSED',
  'VOTING_OPEN',
  'COMPLETED',
  'CANCELLED',
]

function AdminDashboardPage() {
  const [dashboard, setDashboard] = useState<AdminDashboardData | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<unknown>(null)

  useEffect(() => {
    const abortController = new AbortController()

    async function loadDashboard() {
      setError(null)
      setIsLoading(true)

      try {
        const [elections, districts] = await Promise.all([
          getElections(abortController.signal),
          getVotingDistricts(abortController.signal),
        ])

        if (!abortController.signal.aborted) {
          setDashboard({ elections, districts })
        }
      } catch (requestError) {
        if (!abortController.signal.aborted) {
          setError(requestError)
        }
      } finally {
        if (!abortController.signal.aborted) {
          setIsLoading(false)
        }
      }
    }

    void loadDashboard()

    return () => abortController.abort()
  }, [])

  return (
    <section aria-labelledby="admin-dashboard-heading">
      <h1 id="admin-dashboard-heading">Admin dashboard</h1>

      {isLoading && <p role="status">Loading administration summary...</p>}

      {!isLoading && error !== null && (
        <p role="alert">
          {getErrorMessage(error, 'Unable to load the admin dashboard.')}
        </p>
      )}

      {!isLoading && error === null && dashboard !== null && (
        <>
          <section aria-labelledby="election-summary-heading">
            <h2 id="election-summary-heading">Election summary</h2>
            <dl>
              <dt>Total elections</dt>
              <dd>{formatNumber(dashboard.elections.length)}</dd>

              {ELECTION_STATUSES.map((status) => (
                <div key={status}>
                  <dt>{formatEnumLabel(status)}</dt>
                  <dd>
                    {formatNumber(
                      dashboard.elections.filter(
                        (election) => election.status === status,
                      ).length,
                    )}
                  </dd>
                </div>
              ))}
            </dl>
          </section>

          <section aria-labelledby="district-summary-heading">
            <h2 id="district-summary-heading">Voting districts</h2>
            <p>{formatNumber(dashboard.districts.length)} districts available.</p>
          </section>

          <section aria-labelledby="admin-tools-heading">
            <h2 id="admin-tools-heading">Admin tools</h2>
            <ul>
              <li>
                <Link to="/admin/security-events">
                  Review security audit events
                </Link>
              </li>
            </ul>
          </section>
        </>
      )}
    </section>
  )
}

export default AdminDashboardPage
