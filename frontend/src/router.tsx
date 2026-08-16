import { createBrowserRouter } from 'react-router'
import App from './App'
import RequireVoter from './auth/RequireVoter'
import ContestAuditPage from './pages/ContestAuditPage'
import ContestLedgerPage from './pages/ContestLedgerPage'
import ContestResultsPage from './pages/ContestResultsPage'
import ElectionDetailsPage from './pages/ElectionDetailsPage'
import ElectionRegistrationPage from './pages/ElectionRegistrationPage'
import ElectionsPage from './pages/ElectionsPage'
import HomePage from './pages/HomePage'
import LoginPage from './pages/LoginPage'
import NotFoundPage from './pages/NotFoundPage'
import RegisterPage from './pages/RegisterPage'
import RouteErrorPage from './pages/RouteErrorPage'
import VoterDashboardPage from './pages/VoterDashboardPage'

const router = createBrowserRouter([
  {
    path: '/',
    element: <App />,
    errorElement: <RouteErrorPage />,
    children: [
      {
        index: true,
        element: <HomePage />,
      },
      {
        path: 'elections',
        element: <ElectionsPage />,
      },
      {
        path: 'elections/:electionId',
        element: <ElectionDetailsPage />,
      },
      {
        path: 'elections/:electionId/contests/:contestId/results',
        element: <ContestResultsPage />,
      },
      {
        path: 'elections/:electionId/contests/:contestId/audit',
        element: <ContestAuditPage />,
      },
      {
        path: 'elections/:electionId/contests/:contestId/ledger',
        element: <ContestLedgerPage />,
      },
      {
        path: 'login',
        element: <LoginPage />,
      },
      {
        path: 'register',
        element: <RegisterPage />,
      },
      {
        element: <RequireVoter />,
        children: [
          {
            path: 'dashboard',
            element: <VoterDashboardPage />,
          },
          {
            path: 'elections/:electionId/register',
            element: <ElectionRegistrationPage />,
          },
        ],
      },
      {
        path: '*',
        element: <NotFoundPage />,
      },
    ],
  },
])

export default router
