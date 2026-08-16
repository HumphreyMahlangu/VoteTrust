interface LeaveVotingPageWarningProps {
  onStay: () => void
  onLeave: () => void
}

function LeaveVotingPageWarning({
  onStay,
  onLeave,
}: LeaveVotingPageWarningProps) {
  return (
    <section aria-labelledby="leave-warning-heading">
      <h2 id="leave-warning-heading">Leave voting page?</h2>
      <p role="alert">
        Leaving now will discard the only in-browser copy of your one-time
        voting credential. You may be unable to complete this ballot.
      </p>
      <button type="button" onClick={onStay}>
        Stay on voting page
      </button>
      <button type="button" onClick={onLeave}>
        Leave and discard credential
      </button>
    </section>
  )
}

export default LeaveVotingPageWarning
