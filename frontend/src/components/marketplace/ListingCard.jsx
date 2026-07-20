import { formatCurrency, formatTonnes } from '../../utils/formatters';
import styles from './ListingCard.module.css';

function ListingCard({ listing }) {
  const badgeText = listing.standardBadge || 'VERIFIED STANDARD';

  return (
    <div className={styles.card}>
      <div className={styles.header}>
        <div>
          <h3 className={styles.projectName}>{listing.projectName}</h3>
          <span className={styles.sellerName}>Seller: {listing.sellerName || 'Verified Credit Provider'}</span>
        </div>
        <span className={styles.badge}>{badgeText}</span>
      </div>

      <div className={styles.statsRow}>
        <div className={styles.statItem}>
          <span className={styles.statLabel}>Price per Tonne</span>
          <span className={styles.statValue}>{formatCurrency(listing.pricePerTonneUsd)}</span>
        </div>
        <div className={styles.statItem}>
          <span className={styles.statLabel}>Available Credits</span>
          <span className={styles.statValue}>{formatTonnes(listing.availableTonnes)}</span>
        </div>
        <div className={styles.statItem}>
          <span className={styles.statLabel}>Project Type</span>
          <span className={styles.statValue}>{listing.projectType || 'REFORESTATION'}</span>
        </div>
        <div className={styles.statItem}>
          <span className={styles.statLabel}>Vintage Year</span>
          <span className={styles.statValue}>{listing.vintageYear || 2025}</span>
        </div>
      </div>

      <p className={styles.description}>{listing.description}</p>
    </div>
  );
}

export default ListingCard;
