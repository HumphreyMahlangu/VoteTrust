import { Navigate, Outlet } from 'react-router'
import { useAuth } from './useAuth'

function RequireVoter() {
  const { session } = useAuth()

  if (!session) {
    return <Navigate to="/login" replace />
  }

  if (session.role !== 'VOTER') {
    return <Navigate to="/" replace />
  }

  return <Outlet />
}

export default RequireVoter
