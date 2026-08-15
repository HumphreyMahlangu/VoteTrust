const DEFAULT_ERROR_MESSAGE = 'An unexpected error occurred.'

export function getErrorMessage(
  error: unknown,
  fallback = DEFAULT_ERROR_MESSAGE,
) {
  if (error instanceof Error && error.message.trim()) {
    return error.message
  }

  return fallback
}
