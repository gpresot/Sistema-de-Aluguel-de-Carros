import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from './context'

export default function ProtectedRoute() {
  const { sessao } = useAuth()
  const location = useLocation()
  if (!sessao) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />
  }
  return <Outlet />
}
