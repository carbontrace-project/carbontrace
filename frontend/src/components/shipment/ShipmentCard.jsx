import { useNavigate } from 'react-router-dom';
import { formatEmissions, formatTonnes } from '../../utils/formatters';
import styles from './ShipmentCard.module.css';

function ShipmentCard({ shipment }) {
  const navigate = useNavigate();

  const getStatusBadge = (status) => {
    switch (status) {
      case 'UPLOADED':
        return <span className={`${styles.badge} ${styles.badgeUploaded}`}>Uploaded</span>;
      case 'NEEDS_REVIEW':
        return <span className={`${styles.badge} ${styles.badgeNeedsReview}`}>Needs Review</span>;
      case 'REVIEWED':
        return <span className={`${styles.badge} ${styles.badgeReviewed}`}>Reviewed</span>;
      case 'CALCULATED':
        return <span className={`${styles.badge} ${styles.badgeCalculated}`}>Calculated</span>;
      case 'FAILED':
        return <span className={`${styles.badge} ${styles.badgeFailed}`}>Failed</span>;
      default:
        return <span className={styles.badge}>{status}</span>;
    }
  };

  const routeStr =
    shipment.originCity && shipment.destinationCity
      ? `${shipment.originCity}, ${shipment.originCountry || ''} → ${shipment.destinationCity}, ${shipment.destinationCountry || ''}`
      : 'Route details pending review';

  return (
    <div className={styles.card} onClick={() => navigate(`/shipments/${shipment.id}`)}>
      <div className={styles.left}>
        <div className={styles.modeIcon}>
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <rect x="1" y="3" width="15" height="13" />
            <polygon points="16 8 20 8 23 11 23 16 16 16 16 8" />
            <circle cx="5.5" cy="18.5" r="2.5" />
            <circle cx="18.5" cy="18.5" r="2.5" />
          </svg>
        </div>
        <div className={styles.info}>
          <span className={styles.invoiceNumber}>
            {shipment.invoiceNumber || `Shipment #${shipment.id}`}
          </span>
          <span className={styles.route}>{routeStr}</span>
        </div>
      </div>

      <div className={styles.right}>
        {shipment.status === 'CALCULATED' && shipment.totalEmissionsKgco2e ? (
          <span className={styles.emissions}>{formatEmissions(shipment.totalEmissionsKgco2e)}</span>
        ) : (
          <span className={styles.route}>
            {shipment.weightTonnes ? formatTonnes(shipment.weightTonnes) : 'Pending weight'}
          </span>
        )}
        {getStatusBadge(shipment.status)}
      </div>
    </div>
  );
}

export default ShipmentCard;
