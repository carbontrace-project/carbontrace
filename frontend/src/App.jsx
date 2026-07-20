import { createBrowserRouter, RouterProvider, Outlet } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import Navbar from './components/common/Navbar';
import Sidebar from './components/common/Sidebar';
import ProtectedRoute, { AdminRoute } from './components/common/ProtectedRoute';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import VerifyOtpPage from './pages/VerifyOtpPage';
import ForgotPasswordPage from './pages/ForgotPasswordPage';
import VendorsPage from './pages/VendorsPage';
import UploadShipmentPage from './pages/UploadShipmentPage';
import ShipmentsPage from './pages/ShipmentsPage';
import ShipmentDetailPage from './pages/ShipmentDetailPage';
import styles from './styles/App.module.css';

/**
 * Main application layout shell with sticky top Navbar and left Sidebar navigation.
 */
function MainLayout() {
  return (
    <div className={styles.layoutWrapper}>
      <Navbar />
      <div className={styles.layoutBody}>
        <Sidebar />
        <main className={styles.mainContent}>
          <Outlet />
        </main>
      </div>
    </div>
  );
}

/**
 * Welcome / Dashboard shell component for STEP B005 verification.
 */
function Home() {
  const { user } = useAuth();

  const handleCheckConfig = () => {
    const apiBaseUrl = import.meta.env.VITE_API_BASE_URL;
    alert(`Configuration Active:\nAPI Base URL: ${apiBaseUrl}`);
  };

  return (
    <div className={styles.container}>
      <main className={styles.mainCard}>
        <h1 className={styles.title}>CarbonTrace Dashboard</h1>
        <p className={styles.description}>
          Welcome{user?.firstName ? `, ${user.firstName}` : ''}! Core auditor working surface active: Vendors, Upload Flow (S3 direct), Shipments List/Detail, AI Extraction Review, Emission Calculation, and Offset Purchasing (STEP B005).
        </p>
        <div className={styles.buttonGroup}>
          <button type="button" className="btn-primary" onClick={handleCheckConfig}>
            Verify API Base URL Config
          </button>
        </div>
      </main>
    </div>
  );
}

/**
 * Admin shell component placeholder for STEP B005 verification.
 */
function AdminShell() {
  return (
    <div className={styles.container}>
      <main className={styles.mainCard}>
        <h1 className={styles.title}>Admin Control Panel</h1>
        <p className={styles.description}>
          Admin route guard verified. Full admin management pages will be attached in STEP B006.
        </p>
      </main>
    </div>
  );
}

// Complete application router table
const router = createBrowserRouter([
  {
    path: '/login',
    element: <LoginPage />,
  },
  {
    path: '/register',
    element: <RegisterPage />,
  },
  {
    path: '/verify-otp',
    element: <VerifyOtpPage />,
  },
  {
    path: '/forgot-password',
    element: <ForgotPasswordPage />,
  },
  {
    path: '/',
    element: (
      <ProtectedRoute>
        <MainLayout />
      </ProtectedRoute>
    ),
    children: [
      {
        index: true,
        element: <Home />,
      },
      {
        path: 'vendors',
        element: <VendorsPage />,
      },
      {
        path: 'upload',
        element: <UploadShipmentPage />,
      },
      {
        path: 'shipments',
        element: <ShipmentsPage />,
      },
      {
        path: 'shipments/:id',
        element: <ShipmentDetailPage />,
      },
      {
        path: 'admin',
        element: (
          <AdminRoute>
            <AdminShell />
          </AdminRoute>
        ),
      },
    ],
  },
]);

function App() {
  return (
    <AuthProvider>
      <RouterProvider router={router} />
    </AuthProvider>
  );
}

export default App;
