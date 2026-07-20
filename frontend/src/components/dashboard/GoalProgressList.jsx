import { formatNumber } from '../../utils/formatters';
import styles from './GoalProgressList.module.css';

function GoalProgressList({ goals = [] }) {
  if (!goals || goals.length === 0) {
    return (
      <div className={styles.card}>
        <h3 className={styles.title}>Emissions Reduction Goals</h3>
        <p className={styles.emptyText}>
          No active reduction goals defined yet.
        </p>
      </div>
    );
  }

  return (
    <div className={styles.card}>
      <h3 className={styles.title}>Emissions Reduction Goals</h3>
      <div className={styles.list}>
        {goals.map((g) => {
          const progress = Math.min(100, Math.max(0, g.progressPercentage || 0));
          return (
            <div key={g.id} className={styles.goalItem}>
              <div className={styles.goalHeader}>
                <span className={styles.goalTitle}>{g.title}</span>
                <span className={styles.goalPercent}>
                  {formatNumber(progress, 1)}% Achieved (Target: {g.targetReductionPercentage}%)
                </span>
              </div>
              <div className={styles.progressTrack}>
                <div className={styles.progressFill} style={{ width: `${progress}%` }} />
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

export default GoalProgressList;
