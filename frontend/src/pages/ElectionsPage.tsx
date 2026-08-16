import { useEffect, useState } from 'react'
import { getElections } from '../api/elections'
import type { Election } from '../types/election'
import { formatDateTime, formatEnumLabel } from '../utils/formatters'
import { getErrorMessage } from '../utils/getErrorMessage'

function ElectionsPage() {
  const [elections, setElections] = useState<Election[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<unknown>(null)

  useEffect(() => {
    const abortController = new AbortController()

    async function loadElections() {
      try {
        const response = await getElections(abortController.signal)

        if (!abortController.signal.aborted) {
          setElections(response)
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

    void loadElections()

    return () => abortController.abort()
  }, [])

  return (
    <section aria-labelledby="elections-heading">
      <h1 id="elections-heading">Elections</h1>

      {isLoading && <p role="status">Loading elections...</p>}

      {!isLoading && error !== null && (
        <p role="alert">
          {getErrorMessage(error, 'Unable to load elections.')}
        </p>
      )}

      {!isLoading && error === null && elections.length === 0 && (
        <p>No elections are currently available.</p>
      )}

      {!isLoading && error === null && elections.length > 0 && (
        <ul>
          {elections.map((election) => (
            <li key={election.id}>
              <article aria-labelledby={`election-${election.id}`}>
                <h2 id={`election-${election.id}`}>{election.name}</h2>
                <dl>
                  <dt>Type</dt>
                  <dd>{formatEnumLabel(election.type)}</dd>

                  <dt>Status</dt>
                  <dd>{formatEnumLabel(election.status)}</dd>

                  <dt>Registration opens</dt>
                  <dd>
                    <time dateTime={election.registrationStartAt}>
                      {formatDateTime(election.registrationStartAt)}
                    </time>
                  </dd>

                  <dt>Registration closes</dt>
                  <dd>
                    <time dateTime={election.registrationEndAt}>
                      {formatDateTime(election.registrationEndAt)}
                    </time>
                  </dd>

                  <dt>Voting opens</dt>
                  <dd>
                    <time dateTime={election.votingStartAt}>
                      {formatDateTime(election.votingStartAt)}
                    </time>
                  </dd>

                  <dt>Voting closes</dt>
                  <dd>
                    <time dateTime={election.votingEndAt}>
                      {formatDateTime(election.votingEndAt)}
                    </time>
                  </dd>
                </dl>
              </article>
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}

export default ElectionsPage
