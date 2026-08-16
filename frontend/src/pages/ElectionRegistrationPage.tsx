import { useEffect, useState, type FormEvent } from 'react'
import { Link, useParams } from 'react-router'
import ApiError from '../api/ApiError'
import { getVotingDistricts } from '../api/districts'
import { getElection } from '../api/elections'
import { registerForElection } from '../api/registrations'
import { useAuth } from '../auth/useAuth'
import ElectionMetadata from '../components/ElectionMetadata'
import type { VotingDistrict } from '../types/district'
import type { Election } from '../types/election'
import type {
  ElectionRegistration,
  IdDocumentType,
} from '../types/registration'
import { formatDateTime, formatEnumLabel } from '../utils/formatters'
import { getErrorMessage } from '../utils/getErrorMessage'

const documentTypes: IdDocumentType[] = [
  'GREEN_BARCODED_ID',
  'SMART_ID_CARD',
  'TEMPORARY_ID_CERTIFICATE',
]

interface RegistrationPageData {
  election: Election
  districts: VotingDistrict[]
}

function ElectionRegistrationPage() {
  const { electionId } = useParams<{ electionId: string }>()
  const { session, logout } = useAuth()
  const [pageData, setPageData] = useState<RegistrationPageData | null>(null)
  const [southAfricanIdNumber, setSouthAfricanIdNumber] = useState('')
  const [idDocumentType, setIdDocumentType] =
    useState<IdDocumentType>('SMART_ID_CARD')
  const [votingDistrictId, setVotingDistrictId] = useState('')
  const [registration, setRegistration] =
    useState<ElectionRegistration | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState<unknown>(null)
  const [fieldErrors, setFieldErrors] = useState<
    Readonly<Record<string, string>>
  >({})

  useEffect(() => {
    if (!electionId) {
      setError(new Error('Election identifier is missing.'))
      setIsLoading(false)
      return
    }

    const requestedElectionId = electionId
    const abortController = new AbortController()

    async function loadRegistrationPage() {
      setPageData(null)
      setError(null)
      setIsLoading(true)

      try {
        const [election, districtResponse] = await Promise.all([
          getElection(requestedElectionId, abortController.signal),
          getVotingDistricts(abortController.signal),
        ])
        const districts = districtResponse.toSorted((left, right) =>
          `${left.province}-${left.municipality}-${left.code}`.localeCompare(
            `${right.province}-${right.municipality}-${right.code}`,
          ),
        )

        if (!abortController.signal.aborted) {
          setPageData({ election, districts })
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

    void loadRegistrationPage()

    return () => abortController.abort()
  }, [electionId])

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (!electionId || !session) {
      return
    }

    setError(null)
    setFieldErrors({})
    setIsSubmitting(true)

    try {
      const response = await registerForElection(
        electionId,
        { southAfricanIdNumber, idDocumentType, votingDistrictId },
        session.accessToken,
      )
      setSouthAfricanIdNumber('')
      setRegistration(response)
    } catch (requestError) {
      if (requestError instanceof ApiError && requestError.status === 401) {
        logout()
        return
      }

      setError(requestError)

      if (requestError instanceof ApiError) {
        setFieldErrors(requestError.fieldErrors)
      }
    } finally {
      setIsSubmitting(false)
    }
  }

  const electionPath = electionId ? `/elections/${electionId}` : '/elections'
  const idNumberError = fieldErrors.southAfricanIdNumber
  const documentTypeError = fieldErrors.idDocumentType
  const districtError = fieldErrors.votingDistrictId

  return (
    <section aria-labelledby="election-registration-heading">
      <Link to={electionPath}>Back to election</Link>
      <h1 id="election-registration-heading">Election registration</h1>

      {isLoading && <p role="status">Loading registration details...</p>}

      {!isLoading && pageData === null && error !== null && (
        <p role="alert">
          {getErrorMessage(error, 'Unable to load election registration.')}
        </p>
      )}

      {!isLoading && pageData !== null && (
        <>
          <section aria-labelledby="registration-election-heading">
            <h2 id="registration-election-heading">
              {pageData.election.name}
            </h2>
            <ElectionMetadata election={pageData.election} />
          </section>

          {registration !== null ? (
            <section aria-labelledby="registration-complete-heading">
              <h2 id="registration-complete-heading">
                Registration complete
              </h2>
              <p>Your election registration was accepted.</p>
              <dl>
                <dt>Election</dt>
                <dd>{registration.electionName}</dd>

                <dt>Status</dt>
                <dd>{formatEnumLabel(registration.status)}</dd>

                <dt>Voting district</dt>
                <dd>
                  {registration.votingDistrictName} ({
                    registration.votingDistrictCode
                  })
                </dd>

                <dt>Registered</dt>
                <dd>
                  <time dateTime={registration.registeredAt}>
                    {formatDateTime(registration.registeredAt)}
                  </time>
                </dd>
              </dl>
              <Link to="/dashboard">Return to voter dashboard</Link>
            </section>
          ) : pageData.election.status !== 'REGISTRATION_OPEN' ? (
            <p>Registration is not currently open for this election.</p>
          ) : (
            <form onSubmit={handleSubmit} autoComplete="off">
              {error !== null && (
                <p role="alert">
                  {getErrorMessage(
                    error,
                    'Unable to register for this election.',
                  )}
                </p>
              )}

              <div>
                <label htmlFor="south-african-id-number">
                  South African ID number
                </label>
                <p id="id-number-help">
                  Enter all 13 digits. The API stores only a protected hash of
                  this number.
                </p>
                <input
                  id="south-african-id-number"
                  name="southAfricanIdNumber"
                  type="text"
                  inputMode="numeric"
                  pattern="[0-9]{13}"
                  required
                  minLength={13}
                  maxLength={13}
                  autoComplete="off"
                  value={southAfricanIdNumber}
                  aria-invalid={idNumberError ? true : undefined}
                  aria-describedby={
                    idNumberError
                      ? 'id-number-help id-number-error'
                      : 'id-number-help'
                  }
                  onChange={(event) =>
                    setSouthAfricanIdNumber(event.target.value)
                  }
                />
                {idNumberError && <p id="id-number-error">{idNumberError}</p>}
              </div>

              <div>
                <label htmlFor="id-document-type">Identity document type</label>
                <select
                  id="id-document-type"
                  name="idDocumentType"
                  required
                  value={idDocumentType}
                  aria-invalid={documentTypeError ? true : undefined}
                  aria-describedby={
                    documentTypeError ? 'document-type-error' : undefined
                  }
                  onChange={(event) =>
                    setIdDocumentType(event.target.value as IdDocumentType)
                  }
                >
                  {documentTypes.map((documentType) => (
                    <option key={documentType} value={documentType}>
                      {formatEnumLabel(documentType)}
                    </option>
                  ))}
                </select>
                {documentTypeError && (
                  <p id="document-type-error">{documentTypeError}</p>
                )}
              </div>

              <div>
                <label htmlFor="voting-district">Voting district</label>
                <select
                  id="voting-district"
                  name="votingDistrictId"
                  required
                  value={votingDistrictId}
                  aria-invalid={districtError ? true : undefined}
                  aria-describedby={
                    districtError ? 'voting-district-error' : undefined
                  }
                  onChange={(event) =>
                    setVotingDistrictId(event.target.value)
                  }
                >
                  <option value="">Select your voting district</option>
                  {pageData.districts.map((district) => (
                    <option key={district.id} value={district.id}>
                      {district.code} — {district.name}, {district.municipality},
                      ward {district.wardNumber}
                    </option>
                  ))}
                </select>
                {districtError && (
                  <p id="voting-district-error">{districtError}</p>
                )}
                {pageData.districts.length === 0 && (
                  <p>No voting districts are currently available.</p>
                )}
              </div>

              <button
                type="submit"
                disabled={isSubmitting || pageData.districts.length === 0}
              >
                {isSubmitting ? 'Registering...' : 'Register for election'}
              </button>
            </form>
          )}
        </>
      )}
    </section>
  )
}

export default ElectionRegistrationPage
