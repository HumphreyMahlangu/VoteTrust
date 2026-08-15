import { Link, Outlet } from 'react-router'

function App() {
  return (
    <>
      <header>
        <nav aria-label="Primary navigation">
          <Link to="/">VoteTrust</Link>
        </nav>
      </header>

      <main>
        <Outlet />
      </main>
    </>
  )
}

export default App
