import type { ApiErrorResponse } from './ApiErrorResponse'

class ApiError extends Error {
  public readonly status: number
  public readonly error: string
  public readonly path: string
  public readonly timestamp: string
  public readonly fieldErrors: Readonly<Record<string, string>>

  constructor(response: ApiErrorResponse) {
    super(response.message)
    this.name = 'ApiError'
    this.status = response.status
    this.error = response.error
    this.path = response.path
    this.timestamp = response.timestamp
    this.fieldErrors = response.fieldErrors
  }
}

export default ApiError
