import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import styles from './Navbar.module.css';

function Navbar() {
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const roleDisplay = user?.role === 'ROLE_ADMIN' ? 'Admin' : 'Auditor';
  const roleClass = user?.role === 'ROLE_ADMIN' ? styles.roleAdmin : styles.roleAuditor;

  return (
    <header className={styles.header}>
      <Link to="/" className={styles.brand}>
        <svg
          className={styles.logoIcon}
          viewBox="0 0 24 24"
          xmlns="http://www.w3.org/2000/svg"
          aria-label="CarbonTrace Logo"
        >
          <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 17.93c-3.95-.49-7-3.85-7-7.93 0-.62.08-1.21.21-1.79L9 15v1c0 1.1.9 2 2 2v1.93zm6.9-2.54c-.26-.81-1-1.39-1.9-1.39h-1v-3c0-.55-.45-1-1-1H8v-2h2c.55 0 1-.45 1-1V7h2c1.1 0 2-.9 2-2v-.41c2.93 1.19 5 4.06 5 7.41 0 2.08-.8 3.97-2.1 5.39z" />
        </svg>
        <span className={styles.brandTitle}>CarbonTrace</span>
      </Link>

      <div className={styles.centerSection}>
        <span className={styles.simulatedBadge}>
          <span className={styles.badgeDot} />
          Simulated Marketplace — Demo Data
        </span>
      </div>

      <div className={styles.userControls}>
        {isAuthenticated && user ? (
          <>
            <div className={styles.userInfo}>
              <span className={styles.userEmail}>{user.email}</span>
              <span className={`${styles.roleBadge} ${roleClass}`}>{roleDisplay}</span>
            </div>
            <button type="button" className={styles.logoutBtn} onClick={handleLogout}>
              Logout
            </button>
          </>
        ) : (
          <Link to="/login" className={styles.loginLink}>
            Login
          </Link>
        )}
      </div>
    </header>
  );
}

export default Navbar;
