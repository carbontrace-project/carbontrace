import { formatCurrency, formatTonnes } from '../../utils/formatters';
import styles from './PurchaseResultModal.module.css';

function PurchaseResultModal({ isOpen, onClose, purchaseResult = null }) {
  if (!isOpen || !purchaseResult) return null;

  const isSuccess =
    purchaseResult.decision === 'PURCHASE' ||
    purchaseResult.status === 'COMPLETED' ||
    Boolean(purchaseResult.transactionReference);

  const reasoning = purchaseResult.agentReasoning || purchaseResult.reasoning || 'No agent reasoning provided.';

  return (
    <div className={styles.overlay} onClick={onClose}>
      <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
        <div className={styles.header}>
          <h3 className={styles.title}>
            {isSuccess ? 'Carbon Offset Purchase Receipt' : 'Offset Purchase Result'}
          </h3>
          <button type="button" className={styles.closeBtn} onClick={onClose} aria-label="Close modal">
            &times;
          </button>
        </div>

        <div className={styles.simulatedBanner}>
          SIMULATED PURCHASE — no real transaction occurred
        </div>

        <div className={styles.body}>
          {isSuccess ? (
            <>
              <div className={styles.grid}>
                <div className={styles.detailItem}>
                  <span className={styles.label}>Project Name</span>
                  <span className={styles.value}>{purchaseResult.projectName || 'Carbon Offset Project'}</span>
                </div>
                <div className={styles.detailItem}>
                  <span className={styles.label}>Seller Name</span>
                  <span className={styles.value}>{purchaseResult.sellerName || 'Verified Offset Seller'}</span>
                </div>
                <div className={styles.detailItem}>
                  <span className={styles.label}>Tonnes Purchased</span>
                  <span className={styles.value}>{formatTonnes(purchaseResult.tonnesPurchased || purchaseResult.tonnes)}</span>
                </div>
                <div className={styles.detailItem}>
                  <span className={styles.label}>Total Cost</span>
                  <span className={styles.value}>{formatCurrency(purchaseResult.totalCostUsd)}</span>
                </div>
                <div className={`${styles.detailItem} ${styles.fullWidthItem}`}>
                  <span className={styles.label}>SIM Reference Number</span>
                  <span className={styles.refCode}>{purchaseResult.transactionReference || 'SIM-9F3A2C71'}</span>
                </div>
              </div>

              <div className={styles.reasoningBox}>
                <span className={styles.reasoningTitle}>Autonomous AI Agent Reasoning</span>
                <p className={styles.reasoningText}>{reasoning}</p>
              </div>
            </>
          ) : (
            <>
              <div className={styles.noPurchaseBanner}>
                No Viable Offset Purchase Option Found
              </div>
              <div className={styles.reasoningBox}>
                <span className={styles.reasoningTitle}>Agent Explanation</span>
                <p className={styles.reasoningText}>{reasoning}</p>
              </div>
            </>
          )}
        </div>

        <div className={styles.footer}>
          <button type="button" className="btn-primary" onClick={onClose}>
            Close Receipt
          </button>
        </div>
      </div>
    </div>
  );
}

export default PurchaseResultModal;
