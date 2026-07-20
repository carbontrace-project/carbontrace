import { useState, useEffect, useCallback } from 'react';
import apiClient from '../api/axiosConfig';
import EmissionsMap from '../components/emissions/EmissionsMap';
import LoadingSpinner from '../components/common/LoadingSpinner';
import ErrorMessage from '../components/common/ErrorMessage';
import styles from './EmissionsMapPage.module.css';

function EmissionsMapPage() {
  const [shipments, setShipments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchShipments = useCallback(async () => {
    setLoading(true);
    setError(null);

    try {
      let data = [];
      try {
        const response = await apiClient.get('/api/shipments/map');
        data = response.data?.data || response.data || [];
      } catch {
        const response = await apiClient.get('/api/shipments?page=0&size=100');
        data = response.data?.data?.content || response.data?.content || response.data?.data || [];
      }
      setShipments(Array.isArray(data) ? data : []);
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Failed to load map shipments.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchShipments();
  }, [fetchShipments]);

  return (
    <div className={styles.container}>
      <header className={styles.header}>
        <h1 className={styles.title}>Global Logistics GIS Map</h1>
        <p className={styles.subtitle}>
          Interactive spatial visualization of freight transport routes, origin-destination coordinates, and carbon emission intensity.
        </p>
      </header>

      {loading ? (
        <LoadingSpinner message="Loading GIS map routes..." />
      ) : error ? (
        <ErrorMessage title="Map Load Error" message={error} onRetry={fetchShipments} />
      ) : (
        <EmissionsMap shipments={shipments} />
      )}
    </div>
  );
}

export default EmissionsMapPage;
