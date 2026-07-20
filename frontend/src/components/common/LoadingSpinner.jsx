import styles from './LoadingSpinner.module.css';

function LoadingSpinner({ message = 'Loading...', fullScreen = false }) {
  return (
    <div className={`${styles.container} ${fullScreen ? styles.fullScreen : ''}`}>
      <div className={styles.spinner} role="status" aria-label="Loading indicator" />
      {message && <p className={styles.message}>{message}</p>}
    </div>
  );
}

export default LoadingSpinner;
