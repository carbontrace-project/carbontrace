import LoadingSpinner from '../common/LoadingSpinner';
import EmptyState from '../common/EmptyState';
import { formatNumber, formatEmissions } from '../../utils/formatters';
import styles from './GoalList.module.css';

function GoalList({ goals = [], onEditGoal, loading = false }) {
  if (loading) {
    return <LoadingSpinner message="Loading reduction goals..." />;
  }

  if (!goals || goals.length === 0) {
    return (
      <EmptyState
        title="No reduction goals defined"
        description="Set target reduction percentages to track progress towards Net-Zero supply chain targets."
      />
    );
  }

  return (
    <div className={styles.grid}>
      {goals.map((g) => {
        const progress = Math.min(100, Math.max(0, g.progressPercentage || 0));

        return (
          <div key={g.id} className={styles.card}>
            <div className={styles.header}>
              <div>
                <h3 className={styles.title}>{g.title}</h3>
                <span className={styles.dates}>
                  {g.startDate || 'N/A'} &rarr; {g.endDate || 'N/A'}
                </span>
              </div>
              <span
                className={`${styles.badge} ${
                  g.isAchieved ? styles.badgeAchieved : styles.badgeProgress
                }`}
              >
                {g.isAchieved ? 'Achieved' : 'In Progress'}
              </span>
            </div>

            <div className={styles.statsRow}>
              <span>Baseline: {formatEmissions(g.baselineEmissionsKgco2e)}</span>
              <span className={styles.targetText}>Target: -{g.targetReductionPercentage}%</span>
            </div>

            <div className={styles.progressTrack}>
              <div className={styles.progressFill} style={{ width: `${progress}%` }} />
            </div>

            <div className={styles.statsRow}>
              <span style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)' }}>
                Progress: {formatNumber(progress, 1)}%
              </span>
              {onEditGoal && (
                <button
                  type="button"
                  className={`btn-secondary ${styles.editBtn}`}
                  onClick={() => onEditGoal(g)}
                >
                  Edit Goal
                </button>
              )}
            </div>
          </div>
        );
      })}
    </div>
  );
}

export default GoalList;
