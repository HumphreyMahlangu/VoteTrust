import type { ContestOption } from '../../types/contest'
import { formatDateTime } from '../../utils/formatters'
import { getErrorMessage } from '../../utils/getErrorMessage'

interface BallotReviewProps {
  option: ContestOption
  isSubmitting: boolean
  hasIssuedCredential: boolean
  credentialExpiresAt: string | null
  error: unknown
  onChangeSelection: () => void
  onSubmit: () => void
}

function BallotReview({
  option,
  isSubmitting,
  hasIssuedCredential,
  credentialExpiresAt,
  error,
  onChangeSelection,
  onSubmit,
}: BallotReviewProps) {
  return (
    <section aria-labelledby="review-ballot-heading">
      <h2 id="review-ballot-heading">Review your selection</h2>
      <p>
        You selected: <strong>{option.name}</strong>
      </p>
      <p>
        Submitting the ballot is final. Do not close, refresh, or leave this
        page after submission begins.
      </p>

      {credentialExpiresAt && (
        <p>
          The temporary voting credential expires at{' '}
          <time dateTime={credentialExpiresAt}>
            {formatDateTime(credentialExpiresAt)}
          </time>
          .
        </p>
      )}

      {error !== null && (
        <p role="alert">
          {getErrorMessage(error, 'The ballot submission was not confirmed.')}
        </p>
      )}

      {!hasIssuedCredential && (
        <button type="button" onClick={onChangeSelection}>
          Change selection
        </button>
      )}
      <button type="button" disabled={isSubmitting} onClick={onSubmit}>
        {isSubmitting
          ? 'Submitting ballot...'
          : hasIssuedCredential
            ? 'Retry ballot submission'
            : 'Confirm and cast ballot'}
      </button>
    </section>
  )
}

export default BallotReview
