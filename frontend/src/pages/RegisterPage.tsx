import { useState, type FormEvent } from 'react'
import { Link, Navigate, useNavigate } from 'react-router'
import ApiError from '../api/ApiError'
import { useAuth } from '../auth/useAuth'
import { getErrorMessage } from '../utils/getErrorMessage'

function RegisterPage() {
  const navigate = useNavigate()
  const { session, register } = useAuth()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState<unknown>(null)
  const [fieldErrors, setFieldErrors] = useState<
    Readonly<Record<string, string>>
  >({})

  if (session) {
    return <Navigate to="/elections" replace />
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError(null)
    setFieldErrors({})
    setIsSubmitting(true)

    try {
      await register({ email, password })
      navigate('/elections', { replace: true })
    } catch (requestError) {
      setError(requestError)

      if (requestError instanceof ApiError) {
        setFieldErrors(requestError.fieldErrors)
      }
    } finally {
      setIsSubmitting(false)
    }
  }

  const emailError = fieldErrors.email
  const passwordError = fieldErrors.password

  return (
    <section aria-labelledby="register-heading">
      <h1 id="register-heading">Create voter account</h1>

      {error !== null && (
        <p role="alert">
          {getErrorMessage(error, 'Unable to create your account.')}
        </p>
      )}

      <form onSubmit={handleSubmit}>
        <div>
          <label htmlFor="register-email">Email address</label>
          <input
            id="register-email"
            name="email"
            type="email"
            autoComplete="email"
            required
            maxLength={320}
            value={email}
            aria-invalid={emailError ? true : undefined}
            aria-describedby={emailError ? 'register-email-error' : undefined}
            onChange={(event) => setEmail(event.target.value)}
          />
          {emailError && <p id="register-email-error">{emailError}</p>}
        </div>

        <div>
          <label htmlFor="register-password">Password</label>
          <p id="register-password-help">
            Use at least 12 characters with an uppercase letter, lowercase
            letter, and number.
          </p>
          <input
            id="register-password"
            name="password"
            type="password"
            autoComplete="new-password"
            required
            minLength={12}
            maxLength={128}
            value={password}
            aria-invalid={passwordError ? true : undefined}
            aria-describedby={
              passwordError
                ? 'register-password-help register-password-error'
                : 'register-password-help'
            }
            onChange={(event) => setPassword(event.target.value)}
          />
          {passwordError && (
            <p id="register-password-error">{passwordError}</p>
          )}
        </div>

        <button type="submit" disabled={isSubmitting}>
          {isSubmitting ? 'Creating account...' : 'Create account'}
        </button>
      </form>

      <p>
        Already registered? <Link to="/login">Sign in</Link>
      </p>
    </section>
  )
}

export default RegisterPage
