import { useState, useEffect, useCallback } from 'react';
import apiClient from '../../api/axiosConfig';
import ExtractionReviewForm from './ExtractionReviewForm';
import PurchaseResultModal from '../marketplace/PurchaseResultModal';
import LoadingSpinner from '../common/LoadingSpinner';
import ErrorMessage from '../common/ErrorMessage';
import { formatEmissions, formatCurrency, formatTonnes, formatNumber } from '../../utils/formatters';
import styles from './ShipmentDetail.module.css';

function ShipmentDetail({ shipment: initialShipment, onShipmentUpdate }) {
  const [shipment, setShipment] = useState(initialShipment);
  const [calcLoading, setCalcLoading] = useState(false);
  const [calcError, setCalcError] = useState(null);

  const [maxBudget, setMaxBudget] = useState('');
  const [purchaseLoading, setPurchaseLoading] = useState(false);
  const [purchaseError, setPurchaseError] = useState(null);
  const [purchaseResult, setPurchaseResult] = useState(null);
  const [isModalOpen, setIsModalOpen] = useState(false);

  const [purchaseHistory, setPurchaseHistory] = useState([]);

  useEffect(() => {
    setShipment(initialShipment);
  }, [initialShipment]);

  // Fetch purchase history for this shipment
  const fetchPurchaseHistory = useCallback(async () => {
    if (!shipment?.id || shipment.status !== 'CALCULATED') return;
    try {
      const res = await apiClient.get(`/api/purchases/shipment/${shipment.id}`);
      const data = res.data?.data || res.data || [];
      setPurchaseHistory(Array.isArray(data) ? data : [data]);
    } catch {
      // History endpoint optional fallback
    }
  }, [shipment?.id, shipment?.status]);

  useEffect(() => {
    fetchPurchaseHistory();
  }, [fetchPurchaseHistory]);

  const handleReviewSaveSuccess = (updatedShipment) => {
    setShipment(updatedShipment);
    if (onShipmentUpdate) onShipmentUpdate(updatedShipment);
  };

  const handleCalculateEmissions = async () => {
    setCalcLoading(true);
    setCalcError(null);

    try {
      const response = await apiClient.post(`/api/shipments/${shipment.id}/calculate`);
      const updated = response.data?.data || response.data;
      setShipment(updated);
      if (onShipmentUpdate) onShipmentUpdate(updated);
    } catch (err) {
      if (err.response?.status === 502) {
        setCalcError('AI service unavailable. Please check the backend connection and try again.');
      } else {
        const msg = err.response?.data?.message || err.message || 'Emissions calculation failed.';
        setCalcError(msg);
      }
    } finally {
      setCalcLoading(false);
    }
  };

  const handlePurchaseOffset = async () => {
    setPurchaseLoading(true);
    setPurchaseError(null);

    try {
      const payload = {
        shipmentId: shipment.id,
        maxBudgetUsd: maxBudget ? Number(maxBudget) : undefined,
      };

      const response = await apiClient.post('/api/purchases', payload);
      const resData = response.data?.data || response.data;

      setPurchaseResult(resData);
      setIsModalOpen(true);

      // Re-fetch shipment and purchase history
      const freshShipmentRes = await apiClient.get(`/api/shipments/${shipment.id}`);
      const freshShipment = freshShipmentRes.data?.data || freshShipmentRes.data;
      setShipment(freshShipment);
      if (onShipmentUpdate) onShipmentUpdate(freshShipment);
      fetchPurchaseHistory();
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Offset purchase failed.';
      setPurchaseError(msg);
    } finally {
      setPurchaseLoading(false);
    }
  };

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

  if (!shipment) return null;

  return (
    <div className={styles.container}>
      <div className={styles.headerCard}>
        <div className={styles.headerInfo}>
          <h2 className={styles.invoiceTitle}>
            Invoice {shipment.invoiceNumber || `#${shipment.id}`}
          </h2>
          <span className={styles.metaText}>
            Vendor ID: #{shipment.vendorId} | Mode: {shipment.transportMode || 'N/A'} | Date:{' '}
            {shipment.shipmentDate || 'N/A'}
          </span>
        </div>
        <div>{getStatusBadge(shipment.status)}</div>
      </div>

      {(shipment.status === 'UPLOADED' ||
        shipment.status === 'NEEDS_REVIEW' ||
        shipment.status === 'FAILED') && (
        <div className={styles.sectionCard}>
          {shipment.status === 'FAILED' && (
            <ErrorMessage
              title="Extraction Failed"
              message={
                shipment.failureReason ||
                'Document AI extraction failed. Please review and enter the invoice fields manually.'
              }
            />
          )}
          <h3 className={styles.cardTitle}>Audit & Verify Extracted Fields</h3>
          <ExtractionReviewForm shipment={shipment} onSaveSuccess={handleReviewSaveSuccess} />
        </div>
      )}

      {shipment.status === 'REVIEWED' && (
        <div className={styles.sectionCard}>
          <h3 className={styles.cardTitle}>Calculate Carbon Footprint</h3>
          <p className={styles.metaText}>
            Extraction fields have been verified. Click below to compute transport distance and carbon emission footprint against regional GLEC factors.
          </p>

          {calcError && <ErrorMessage title="Calculation Error" message={calcError} />}

          <div>
            <button
              type="button"
              className="btn-primary"
              onClick={handleCalculateEmissions}
              disabled={calcLoading}
            >
              {calcLoading ? <LoadingSpinner message="" /> : 'Calculate Emissions'}
            </button>
          </div>
        </div>
      )}

      {shipment.status === 'CALCULATED' && (
        <div className={styles.sectionCard}>
          <h3 className={styles.cardTitle}>Emissions Summary & Offset Purchase</h3>

          <div className={styles.emissionsBox}>
            <span className={styles.metaText}>Total Carbon Footprint</span>
            <span className={styles.emissionsValue}>
              {formatEmissions(shipment.totalEmissionsKgco2e)}
            </span>
            <span className={styles.formulaString}>
              Formula: Weight ({formatTonnes(shipment.weightTonnes)}) × Distance (
              {formatNumber(shipment.distanceKm, 0)} km) × GLEC Factor (0.011 kgCO₂e/t-km) × Circuity (1.15)
            </span>
          </div>

          <div className={styles.offsetCard}>
            <h4 className={styles.cardSubTitle}>
              Purchase Carbon Credits Offset
            </h4>
            <p className={styles.metaText}>
              Launch our autonomous AI agent to select and purchase verified carbon credits from the marketplace to offset this shipment.
            </p>

            {purchaseError && <ErrorMessage title="Offset Purchase Error" message={purchaseError} />}

            <div className={styles.budgetRow}>
              <div className={styles.budgetGroup}>
                <label htmlFor="max-budget" className={styles.metaText}>
                  Max Budget USD (Optional)
                </label>
                <input
                  id="max-budget"
                  type="number"
                  step="0.01"
                  className={styles.budgetInput}
                  placeholder="e.g. 200.00"
                  value={maxBudget}
                  onChange={(e) => setMaxBudget(e.target.value)}
                />
              </div>

              <button
                type="button"
                className={`btn-primary ${styles.offsetSubmitBtn}`}
                onClick={handlePurchaseOffset}
                disabled={purchaseLoading}
              >
                {purchaseLoading ? <LoadingSpinner message="" /> : 'Offset this shipment ↗'}
              </button>
            </div>
          </div>

          {purchaseHistory.length > 0 && (
            <div>
              <h4 className={styles.cardSubTitle}>
                Purchase History
              </h4>
              <table className={styles.historyTable}>
                <thead>
                  <tr>
                    <th>SIM Ref</th>
                    <th>Project</th>
                    <th>Tonnes</th>
                    <th>Total Cost</th>
                  </tr>
                </thead>
                <tbody>
                  {purchaseHistory.map((p, idx) => (
                    <tr key={p.id || idx}>
                      <td>
                        <strong>{p.transactionReference || 'SIM-9F3A2C71'}</strong>
                      </td>
                      <td>{p.projectName || 'Offset Project'}</td>
                      <td>{formatTonnes(p.tonnesPurchased || p.tonnes)}</td>
                      <td>{formatCurrency(p.totalCostUsd)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      <PurchaseResultModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        purchaseResult={purchaseResult}
      />
    </div>
  );
}

export default ShipmentDetail;
