import { describe, expect, it } from 'vitest'
import { formatDateTime, formatEnumLabel, formatNumber } from './formatters'

describe('formatters', () => {
  it('turns an enum value into a readable label', () => {
    expect(formatEnumLabel('REGISTRATION_OPEN')).toBe('Registration open')
  })

  it('returns an invalid date value unchanged', () => {
    expect(formatDateTime('not-a-date')).toBe('not-a-date')
  })

  it('formats valid timestamps in South African time', () => {
    const formattedDate = formatDateTime('2026-08-16T10:00:00Z')

    expect(formattedDate).toContain('12:00')
    expect(formattedDate.endsWith('SAST')).toBe(true)
  })

  it('formats numbers without changing their value', () => {
    const formattedNumber = formatNumber(1234)
    const digitsOnly = formattedNumber.replaceAll(/\D/g, '')

    expect(digitsOnly).toBe('1234')
  })
})
