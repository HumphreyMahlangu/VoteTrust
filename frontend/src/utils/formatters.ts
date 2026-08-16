const dateTimeFormatter = new Intl.DateTimeFormat('en-ZA', {
  dateStyle: 'medium',
  timeStyle: 'short',
  timeZone: 'Africa/Johannesburg',
})

const numberFormatter = new Intl.NumberFormat('en-ZA')

const percentageFormatter = new Intl.NumberFormat('en-ZA', {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
})

export function formatDateTime(value: string) {
  const date = new Date(value)

  if (Number.isNaN(date.getTime())) {
    return value
  }

  return `${dateTimeFormatter.format(date)} SAST`
}

export function formatEnumLabel(value: string) {
  const label = value.toLowerCase().replaceAll('_', ' ')
  return label.charAt(0).toUpperCase() + label.slice(1)
}

export function formatNumber(value: number) {
  return numberFormatter.format(value)
}

export function formatPercentage(value: number) {
  return `${percentageFormatter.format(value)}%`
}
