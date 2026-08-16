import { Link, Outlet } from 'react-router'
import { useAuth } from './auth/useAuth'

function App() {
  const { session, logout } = useAuth()

  return (
    <>
      <header>
        <nav aria-label="Primary navigation">
          <ul>
            <li>
              <Link to="/">VoteTrust</Link>
            </li>
            <li>
              <Link to="/elections">Elections</Link>
            </li>
            {session ? (
              <>
                {session.role === 'VOTER' && (
                  <li>
                    <Link to="/dashboard">Dashboard</Link>
                  </li>
                )}
                <li>
                  <span>Signed in as {session.email}</span>{' '}
                  <button type="button" onClick={logout}>
                    Sign out
                  </button>
                </li>
              </>
            ) : (
              <>
                <li>
                  <Link to="/login">Sign in</Link>
                </li>
                <li>
                  <Link to="/register">Register</Link>
                </li>
              </>
            )}
          </ul>
        </nav>
      </header>

      <main>
        <Outlet />
      </main>
    </>
  )
}

export default App
