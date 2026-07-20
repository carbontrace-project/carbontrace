import { createBrowserRouter, RouterProvider, Outlet } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import Navbar from './components/common/Navbar';
import Sidebar from './components/common/Sidebar';
import ProtectedRoute, { AdminRoute } from './components/common/ProtectedRoute';
import LoginPage from './pages/LoginPage';
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
 * Welcome / Dashboard shell component for STEP B003 verification.
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
        <h1 className={styles.title}>CarbonTrace Platform Shell</h1>
        <p className={styles.description}>
          Welcome{user?.firstName ? `, ${user.firstName}` : ''}! Authentication foundation, JWT refresh interceptor, route guards, and common components have been initialized (STEP B003).
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
 * Admin shell component placeholder for STEP B003 verification.
 */
function AdminShell() {
  return (
    <div className={styles.container}>
      <main className={styles.mainCard}>
        <h1 className={styles.title}>Admin Control Panel Shell</h1>
        <p className={styles.description}>
          Admin route guard verified successfully. User management and factors management will be attached in feature steps.
        </p>
      </main>
    </div>
  );
}

// Route table pointing at pages created in STEP B003 & STEP B004 shells
const router = createBrowserRouter([
  {
    path: '/login',
    element: <LoginPage />,
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
