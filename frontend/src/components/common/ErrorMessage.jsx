import styles from './ErrorMessage.module.css';

function ErrorMessage({ title = 'Error', message = 'An unexpected error occurred.', onRetry }) {
  return (
    <div className={styles.container} role="alert">
      <div className={styles.iconContainer}>
        <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
          <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z" />
        </svg>
      </div>
      <div className={styles.content}>
        <h4 className={styles.title}>{title}</h4>
        <p className={styles.message}>{message}</p>
        {onRetry && (
          <button type="button" className={styles.retryBtn} onClick={onRetry}>
            Try Again
          </button>
        )}
      </div>
    </div>
  );
}

export default ErrorMessage;
