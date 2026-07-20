import { createBrowserRouter, RouterProvider, Outlet } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import Navbar from './components/common/Navbar';
import Sidebar from './components/common/Sidebar';
import ProtectedRoute, { AdminRoute } from './components/common/ProtectedRoute';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import VerifyOtpPage from './pages/VerifyOtpPage';
import ForgotPasswordPage from './pages/ForgotPasswordPage';
import DashboardPage from './pages/DashboardPage';
import VendorsPage from './pages/VendorsPage';
import UploadShipmentPage from './pages/UploadShipmentPage';
import ShipmentsPage from './pages/ShipmentsPage';
import ShipmentDetailPage from './pages/ShipmentDetailPage';
import EmissionsMapPage from './pages/EmissionsMapPage';
import GoalsPage from './pages/GoalsPage';
import MarketplacePage from './pages/MarketplacePage';
import PurchasesPage from './pages/PurchasesPage';
import AdminPage from './pages/AdminPage';
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
        element: <DashboardPage />,
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
        path: 'emissions-map',
        element: <EmissionsMapPage />,
      },
      {
        path: 'goals',
        element: <GoalsPage />,
      },
      {
        path: 'marketplace',
        element: <MarketplacePage />,
      },
      {
        path: 'purchases',
        element: <PurchasesPage />,
      },
      {
        path: 'admin',
        element: (
          <AdminRoute>
            <AdminPage />
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
