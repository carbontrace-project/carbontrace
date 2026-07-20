import styles from './LoginPage.module.css';

function LoginPage() {
  return (
    <div className={styles.container}>
      <div className={styles.card}>
        <div className={styles.brandHeader}>
          <svg className={styles.logoIcon} viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 17.93c-3.95-.49-7-3.85-7-7.93 0-.62.08-1.21.21-1.79L9 15v1c0 1.1.9 2 2 2v1.93zm6.9-2.54c-.26-.81-1-1.39-1.9-1.39h-1v-3c0-.55-.45-1-1-1H8v-2h2c.55 0 1-.45 1-1V7h2c1.1 0 2-.9 2-2v-.41c2.93 1.19 5 4.06 5 7.41 0 2.08-.8 3.97-2.1 5.39z" />
          </svg>
          <h2>CarbonTrace</h2>
        </div>
        <h3 className={styles.title}>Sign In</h3>
        <p className={styles.subtitle}>Supply Chain Emission Auditing Platform</p>
        <div className={styles.placeholderNote}>
          LoginPage shell initialized in STEP B003. Full interactive authentication forms will be attached in STEP B004.
        </div>
      </div>
    </div>
  );
}

export default LoginPage;
