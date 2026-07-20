import { useState, useEffect, useCallback } from 'react';
import apiClient from '../../api/axiosConfig';
import LoadingSpinner from '../common/LoadingSpinner';
import ErrorMessage from '../common/ErrorMessage';
import { formatNumber } from '../../utils/formatters';
import styles from './EmissionFactorTab.module.css';

function EmissionFactorTab() {
  const [factors, setFactors] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchFactors = useCallback(async () => {
    setLoading(true);
    setError(null);

    try {
      let response;
      try {
        response = await apiClient.get('/api/admin/emission-factors');
      } catch {
        response = await apiClient.get('/api/emission-factors');
      }
      const data = response.data?.data || response.data || [];
      setFactors(Array.isArray(data) ? data : []);
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Failed to load GLEC emission factors.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchFactors();
  }, [fetchFactors]);

  return (
    <div className={styles.container}>
      {error && <ErrorMessage title="Emission Factors Error" message={error} onRetry={fetchFactors} />}

      {loading ? (
        <LoadingSpinner message="Loading GLEC emission factors..." />
      ) : (
        <div className={styles.tableWrapper}>
          <table className={styles.table}>
            <thead>
              <tr>
                <th>Mode</th>
                <th>Fuel Type</th>
                <th>Emission Factor (kgCO₂e / t-km)</th>
                <th>Circuity Factor</th>
                <th>Source Standard</th>
              </tr>
            </thead>
            <tbody>
              {factors.map((f, idx) => (
                <tr key={f.id || idx}>
                  <td>
                    <strong>{f.transportMode}</strong>
                  </td>
                  <td>{f.fuelType}</td>
                  <td>
                    <strong>{formatNumber(f.kgco2ePerTonneKm, 6)}</strong>
                  </td>
                  <td>{formatNumber(f.circuityFactor || 1.15, 2)}</td>
                  <td>{f.source || 'GLEC Framework v3.0'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

export default EmissionFactorTab;
