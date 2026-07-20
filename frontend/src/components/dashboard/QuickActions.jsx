import { useNavigate } from 'react-router-dom';
import styles from './QuickActions.module.css';

function QuickActions() {
  const navigate = useNavigate();

  return (
    <div className={styles.card}>
      <h3 className={styles.title}>Quick Actions</h3>
      <div className={styles.buttonGrid}>
        <button
          type="button"
          className={`btn-primary ${styles.actionBtn}`}
          onClick={() => navigate('/upload')}
        >
          + Upload Freight Bill
        </button>
        <button
          type="button"
          className={`btn-secondary ${styles.actionBtn}`}
          onClick={() => navigate('/emissions-map')}
        >
          🗺️ View GIS Map
        </button>
        <button
          type="button"
          className={`btn-secondary ${styles.actionBtn}`}
          onClick={() => navigate('/marketplace')}
        >
          🌱 Carbon Marketplace
        </button>
      </div>
    </div>
  );
}

export default QuickActions;
