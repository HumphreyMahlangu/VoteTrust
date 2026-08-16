import { Navigate, Outlet } from 'react-router'
import { useAuth } from './useAuth'

function RequireAdmin() {
  const { session } = useAuth()

  if (!session) {
    return <Navigate to="/login" replace />
  }

  if (session.role !== 'ADMIN') {
    return <Navigate to="/dashboard" replace />
  }

  return <Outlet />
}

export default RequireAdmin
