import styles from './EmptyState.module.css';

function EmptyState({
  title = 'No records found',
  description = 'There are no items to display at this time.',
  actionLabel,
  onAction,
  icon,
}) {
  return (
    <div className={styles.container}>
      <div className={styles.iconWrapper}>
        {icon || (
          <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M20 13V6a2 2 0 0 0-2-2H6a2 2 0 0 0-2 2v7m16 0v5a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2v-5m16 0h-4.5a2.5 2.5 0 0 0-2.5 2.5v.5a1 1 0 0 1-1 1h-2a1 1 0 0 1-1-1v-.5A2.5 2.5 0 0 0 6.5 13H2" />
          </svg>
        )}
      </div>
      <h3 className={styles.title}>{title}</h3>
      {description && <p className={styles.description}>{description}</p>}
      {actionLabel && onAction && (
        <button type="button" className={`btn-primary ${styles.actionBtn}`} onClick={onAction}>
          {actionLabel}
        </button>
      )}
    </div>
  );
}

export default EmptyState;
