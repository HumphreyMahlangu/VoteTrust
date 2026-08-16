import { createBrowserRouter } from 'react-router'
import App from './App'
import ElectionDetailsPage from './pages/ElectionDetailsPage'
import ElectionsPage from './pages/ElectionsPage'
import HomePage from './pages/HomePage'
import NotFoundPage from './pages/NotFoundPage'
import RouteErrorPage from './pages/RouteErrorPage'

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
        path: '*',
        element: <NotFoundPage />,
      },
    ],
  },
])

export default router
