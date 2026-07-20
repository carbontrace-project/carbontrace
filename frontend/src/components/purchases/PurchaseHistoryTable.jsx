import { useState, Fragment } from 'react';
import Pagination from '../common/Pagination';
import LoadingSpinner from '../common/LoadingSpinner';
import EmptyState from '../common/EmptyState';
import { formatCurrency, formatTonnes } from '../../utils/formatters';
import styles from './PurchaseHistoryTable.module.css';

function PurchaseHistoryTable({
  purchases = [],
  currentPage = 0,
  totalPages = 1,
  totalElements = 0,
  pageSize = 10,
  onPageChange,
  loading = false,
}) {
  const [expandedId, setExpandedId] = useState(null);

  const toggleExpand = (id) => {
    setExpandedId((prev) => (prev === id ? null : id));
  };

  if (loading) {
    return <LoadingSpinner message="Loading purchase history..." />;
  }

  if (!purchases || purchases.length === 0) {
    return (
      <EmptyState
        title="No simulated purchases recorded"
        description="Completed offset credit purchases will appear here with detailed AI agent decision logs."
      />
    );
  }

  return (
    <div className={styles.container}>
      <div className={styles.tableWrapper}>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>Date</th>
              <th>SIM Ref</th>
              <th>Shipment Invoice</th>
              <th>Project Name</th>
              <th>Tonnes</th>
              <th>Total Cost</th>
              <th>Reasoning</th>
            </tr>
          </thead>
          <tbody>
            {purchases.map((p) => {
              const isExpanded = expandedId === p.id;
              const dateStr = p.purchasedAt ? new Date(p.purchasedAt).toLocaleDateString() : 'N/A';

              return (
                <Fragment key={p.id}>
                  <tr className={styles.rowSelectable} onClick={() => toggleExpand(p.id)}>
                    <td>{dateStr}</td>
                    <td>
                      <span className={styles.refBadge}>
                        {p.transactionReference || 'SIM-9F3A2C71'}
                      </span>
                    </td>
                    <td>
                      <strong>{p.invoiceNumber || `Shipment #${p.shipmentId}`}</strong>
                    </td>
                    <td>{p.projectName || 'Carbon Project'}</td>
                    <td>{formatTonnes(p.tonnesPurchased || p.tonnes)}</td>
                    <td>
                      <strong>{formatCurrency(p.totalCostUsd)}</strong>
                    </td>
                    <td>
                      <button type="button" className={`btn-secondary ${styles.expandBtn}`}>
                        {isExpanded ? 'Hide ▲' : 'View AI Reasoning ▼'}
                      </button>
                    </td>
                  </tr>

                  {isExpanded && (
                    <tr className={styles.expandedRow}>
                      <td colSpan={7}>
                        <div className={styles.reasoningBox}>
                          <span className={styles.reasoningTitle}>Autonomous AI Agent Reasoning Log</span>
                          <p className={styles.reasoningText}>
                            {p.agentReasoning || p.reasoning || 'No agent reasoning provided for this transaction.'}
                          </p>
                        </div>
                      </td>
                    </tr>
                  )}
                </Fragment>
              );
            })}
          </tbody>
        </table>
      </div>

      <Pagination
        currentPage={currentPage}
        totalPages={totalPages}
        totalElements={totalElements}
        pageSize={pageSize}
        onPageChange={onPageChange}
      />
    </div>
  );
}

export default PurchaseHistoryTable;
