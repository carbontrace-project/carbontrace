import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import styles from './styles/App.module.css';

/**
 * Homepage component rendering the minimal application shell.
 * Shows the corporate identity, branding, and verifies that the environment config is active.
 */
function Home() {
  const handleCheckConfig = () => {
    const apiBaseUrl = import.meta.env.VITE_API_BASE_URL;
    alert(`Configuration Active:\nAPI Base URL: ${apiBaseUrl}`);
  };

  return (
    <div className={styles.container}>
      <header className={styles.header}>
        <svg 
          className="brand-icon" 
          viewBox="0 0 24 24" 
          xmlns="http://www.w3.org/2000/svg"
          aria-label="CarbonTrace Logo"
        >
          <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 17.93c-3.95-.49-7-3.85-7-7.93 0-.62.08-1.21.21-1.79L9 15v1c0 1.1.9 2 2 2v1.93zm6.9-2.54c-.26-.81-1-1.39-1.9-1.39h-1v-3c0-.55-.45-1-1-1H8v-2h2c.55 0 1-.45 1-1V7h2c1.1 0 2-.9 2-2v-.41c2.93 1.19 5 4.06 5 7.41 0 2.08-.8 3.97-2.1 5.39z"/>
        </svg>
        <span className={styles.logoText}>
          CarbonTrace
        </span>
      </header>
      <main className={styles.mainCard}>
        <h1 className={styles.title}>
          CarbonTrace Platform Initialized
        </h1>
        <p className={styles.description}>
          Welcome to CarbonTrace, a platform for auditing supply chain greenhouse gas footprints. Auditors can upload freight documents, check AI extraction results, run emissions calculations, and offset footprints.
        </p>
        <div className={styles.buttonGroup}>
          <button 
            type="button" 
            className="btn-primary" 
            onClick={() => alert('CarbonTrace initialized. No additional modules are active yet.')}
          >
            Learn More
          </button>
          <button 
            type="button" 
            className="btn-secondary" 
            onClick={handleCheckConfig}
          >
            Verify API Configuration
          </button>
        </div>
      </main>
    </div>
  );
}

// Router configuration with the single index path per specifications.
const router = createBrowserRouter([
  {
    path: '/',
    element: <Home />,
  },
]);

function App() {
  return <RouterProvider router={router} />;
}

export default App;
