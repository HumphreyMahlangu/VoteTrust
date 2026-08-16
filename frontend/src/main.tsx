import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { RouterProvider } from 'react-router'
import AuthProvider from './auth/AuthProvider'
import router from './router'

const rootElement = document.getElementById('root')

if (!rootElement) {
  throw new Error('Root element was not found')
}

createRoot(rootElement).render(
  <StrictMode>
    <AuthProvider>
      <RouterProvider router={router} />
    </AuthProvider>
  </StrictMode>,
)
