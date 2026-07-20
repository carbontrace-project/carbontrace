import { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import apiClient from '../api/axiosConfig';
import ShipmentDetail from '../components/shipment/ShipmentDetail';
import LoadingSpinner from '../components/common/LoadingSpinner';
import ErrorMessage from '../components/common/ErrorMessage';
import styles from './ShipmentDetailPage.module.css';

function ShipmentDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [shipment, setShipment] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchShipment = useCallback(async () => {
    if (!id) return;
    setLoading(true);
    setError(null);

    try {
      const response = await apiClient.get(`/api/shipments/${id}`);
      const data = response.data?.data || response.data;
      setShipment(data);
    } catch (err) {
      const msg = err.response?.data?.message || err.message || `Failed to fetch shipment #${id}.`;
      setError(msg);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    fetchShipment();
  }, [fetchShipment]);

  return (
    <div className={styles.container}>
      <button type="button" className={`btn-secondary ${styles.backBtn}`} onClick={() => navigate('/shipments')}>
        &larr; Back to Shipments List
      </button>

      {loading ? (
        <LoadingSpinner message={`Loading shipment #${id}...`} />
      ) : error ? (
        <ErrorMessage title="Shipment Load Error" message={error} onRetry={fetchShipment} />
      ) : (
        <ShipmentDetail shipment={shipment} onShipmentUpdate={(updated) => setShipment(updated)} />
      )}
    </div>
  );
}

export default ShipmentDetailPage;
