import { Link, Outlet } from 'react-router'

function App() {
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
