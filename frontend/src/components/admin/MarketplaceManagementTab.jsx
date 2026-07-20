import { useState, useEffect, useCallback } from 'react';
import apiClient from '../../api/axiosConfig';
import LoadingSpinner from '../common/LoadingSpinner';
import ErrorMessage from '../common/ErrorMessage';
import { formatCurrency, formatTonnes } from '../../utils/formatters';
import styles from './MarketplaceManagementTab.module.css';

function MarketplaceManagementTab() {
  const [sellers, setSellers] = useState([]);
  const [listings, setListings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchAdminMarketplace = useCallback(async () => {
    setLoading(true);
    setError(null);

    try {
      let sellersRes;
      try {
        sellersRes = await apiClient.get('/api/admin/marketplace/sellers');
      } catch {
        sellersRes = await apiClient.get('/api/marketplace/sellers');
      }
      setSellers(sellersRes.data?.data?.content || sellersRes.data?.data || sellersRes.data || []);

      let listingsRes;
      try {
        listingsRes = await apiClient.get('/api/marketplace/listings?page=0&size=50');
      } catch {
        listingsRes = await apiClient.get('/api/marketplace/credits?page=0&size=50');
      }
      setListings(listingsRes.data?.data?.content || listingsRes.data?.content || listingsRes.data || []);
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Failed to load marketplace management details.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchAdminMarketplace();
  }, [fetchAdminMarketplace]);

  const handleRestock = async (listingId) => {
    const input = prompt('Enter tonnes to add to this listing:', '1000');
    if (!input || isNaN(Number(input))) return;

    try {
      try {
        await apiClient.put(`/api/admin/marketplace/listings/${listingId}/restock`, {
          additionalTonnes: Number(input),
        });
      } catch {
        await apiClient.put(`/api/admin/marketplace/credits/${listingId}/restock`, {
          additionalTonnes: Number(input),
        });
      }
      fetchAdminMarketplace();
    } catch (err) {
      alert(err.response?.data?.message || err.message || 'Restock failed.');
    }
  };

  if (loading) return <LoadingSpinner message="Loading marketplace admin data..." />;

  return (
    <div className={styles.container}>
      {error && <ErrorMessage title="Marketplace Admin Error" message={error} onRetry={fetchAdminMarketplace} />}

      <h3 className={styles.sectionTitle}>1. Verified Credit Sellers</h3>
      <div className={styles.tableWrapper}>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>ID</th>
              <th>Seller Name</th>
              <th>Registry Identifier</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            {sellers.map((s) => (
              <tr key={s.id}>
                <td>#{s.id}</td>
                <td>
                  <strong>{s.name}</strong>
                </td>
                <td>{s.registryId || 'REG-INT-001'}</td>
                <td>{s.isActive ? 'Active' : 'Inactive'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <h3 className={styles.sectionTitle}>2. Credit Listings Inventory & Restock</h3>
      <div className={styles.tableWrapper}>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>ID</th>
              <th>Project Name</th>
              <th>Price / Tonne</th>
              <th>Available Tonnes</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            {listings.map((l) => (
              <tr key={l.id}>
                <td>#{l.id}</td>
                <td>
                  <strong>{l.projectName}</strong>
                </td>
                <td>{formatCurrency(l.pricePerTonneUsd)}</td>
                <td>
                  <strong>{formatTonnes(l.availableTonnes)}</strong>
                </td>
                <td>
                  <button
                    type="button"
                    className={`btn-secondary ${styles.actionBtn}`}
                    onClick={() => handleRestock(l.id)}
                  >
                    + Restock Tonnes
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

export default MarketplaceManagementTab;
