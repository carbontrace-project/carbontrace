import { Navigate, useLocation, Outlet } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import LoadingSpinner from './LoadingSpinner';
import ErrorMessage from './ErrorMessage';
import styles from './ProtectedRoute.module.css';

export function ProtectedRoute({ children, adminOnly = false }) {
  const { isAuthenticated, role, loading } = useAuth();
  const location = useLocation();

  if (loading) {
    return <LoadingSpinner fullScreen message="Verifying authentication session..." />;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (adminOnly && role !== 'ROLE_ADMIN') {
    return (
      <div className={styles.deniedContainer}>
        <ErrorMessage
          title="403 - Access Denied"
          message="Administrator privileges are required to access this resource."
        />
      </div>
    );
  }

  return children ? children : <Outlet />;
}

export function AdminRoute({ children }) {
  return <ProtectedRoute adminOnly>{children}</ProtectedRoute>;
}

export default ProtectedRoute;
