import { formatEmissions, formatTonnes } from '../../utils/formatters';
import styles from './StatCards.module.css';

function StatCards({ summary = {} }) {
  return (
    <div className={styles.grid}>
      <div className={styles.card}>
        <div className={styles.header}>
          <span className={styles.label}>Gross Emissions</span>
          <svg className={styles.icon} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M12 2v20M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6" />
          </svg>
        </div>
        <span className={styles.value}>{formatEmissions(summary.totalEmissionsKgco2e)}</span>
        <span className={styles.subtext}>Total calculated cargo emissions</span>
      </div>

      <div className={styles.card}>
        <div className={styles.header}>
          <span className={styles.label}>Net Footprint</span>
          <svg className={styles.icon} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <circle cx="12" cy="12" r="10" />
            <path d="M8 14s1.5 2 4 2 4-2 4-2" />
            <line x1="9" y1="9" x2="9.01" y2="9" />
            <line x1="15" y1="9" x2="15.01" y2="9" />
          </svg>
        </div>
        <span className={styles.value}>{formatEmissions(summary.netEmissionsKgco2e)}</span>
        <span className={styles.subtext}>Gross emissions minus offsets</span>
      </div>

      <div className={styles.card}>
        <div className={styles.header}>
          <span className={styles.label}>Shipments Analyzed</span>
          <svg className={styles.icon} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <rect x="1" y="3" width="15" height="13" />
            <polygon points="16 8 20 8 23 11 23 16 16 16 16 8" />
            <circle cx="5.5" cy="18.5" r="2.5" />
            <circle cx="18.5" cy="18.5" r="2.5" />
          </svg>
        </div>
        <span className={styles.value}>{summary.totalShipments || 0}</span>
        <span className={styles.subtext}>
          {summary.calculatedShipments || 0} calculated, {summary.pendingReviewShipments || 0} pending review
        </span>
      </div>

      <div className={styles.card}>
        <div className={styles.header}>
          <span className={styles.label}>Carbon Offsets</span>
          <svg className={styles.icon} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M11 20A7 7 0 0 1 9.8 6.1C15.5 5 17 4.48 19 2c1 2 2 4.18 2 8 0 5.5-4.78 10-10 10Z" />
            <path d="M2 21c0-3 1.85-5.36 5.08-6C9.5 14.52 12 13 13 12" />
          </svg>
        </div>
        <span className={styles.value}>{formatTonnes(summary.totalOffsetTonnes)}</span>
        <span className={styles.subtext}>Purchased verified credits</span>
      </div>

      <div className={styles.card}>
        <div className={styles.header}>
          <span className={styles.label}>Active Partners</span>
          <svg className={styles.icon} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
            <circle cx="9" cy="7" r="4" />
            <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
            <path d="M16 3.13a4 4 0 0 1 0 7.75" />
          </svg>
        </div>
        <span className={styles.value}>{summary.activeVendors || 0}</span>
        <span className={styles.subtext}>Logistics vendors registered</span>
      </div>
    </div>
  );
}

export default StatCards;
