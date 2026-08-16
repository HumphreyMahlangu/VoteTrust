import type { Election } from '../types/election'
import { formatDateTime, formatEnumLabel } from '../utils/formatters'

interface ElectionMetadataProps {
  election: Election
}

function ElectionMetadata({ election }: ElectionMetadataProps) {
  return (
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
  )
}

export default ElectionMetadata
