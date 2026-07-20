import { useState, useEffect, useCallback } from 'react';
import apiClient from '../api/axiosConfig';
import ListingList from '../components/marketplace/ListingList';
import ErrorMessage from '../components/common/ErrorMessage';
import styles from './MarketplacePage.module.css';

function MarketplacePage() {
  const [listings, setListings] = useState([]);
  const [projectTypeFilter, setProjectTypeFilter] = useState('');
  const [maxPriceFilter, setMaxPriceFilter] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchListings = useCallback(async (page = 0, projectType = '', maxPrice = '') => {
    setLoading(true);
    setError(null);

    try {
      let response;
      try {
        response = await apiClient.get('/api/marketplace/listings', {
          params: {
            page,
            size: 9,
            projectType: projectType || undefined,
            maxPriceUsd: maxPrice || undefined,
          },
        });
      } catch {
        response = await apiClient.get('/api/marketplace/credits', {
          params: {
            page,
            size: 9,
            projectType: projectType || undefined,
            maxPricePerTonne: maxPrice || undefined,
          },
        });
      }

      const resData = response.data?.data || response.data;
      const content = resData?.content || resData || [];
      const pageInfo = resData?.page !== undefined ? resData : { page: 0, totalPages: 1, totalElements: content.length };

      setListings(content);
      setCurrentPage(pageInfo.page || 0);
      setTotalPages(pageInfo.totalPages || 1);
      setTotalElements(pageInfo.totalElements || content.length);
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Failed to load carbon credit listings.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchListings(currentPage, projectTypeFilter, maxPriceFilter);
  }, [fetchListings, currentPage, projectTypeFilter, maxPriceFilter]);

  return (
    <div className={styles.container}>
      <header className={styles.header}>
        <h1 className={styles.title}>Carbon Credit Marketplace</h1>
        <p className={styles.subtitle}>
          Browse high-integrity, verified carbon removal and reduction credit listings for autonomous AI offset purchasing.
        </p>
      </header>

      {error && (
        <ErrorMessage
          title="Marketplace Load Error"
          message={error}
          onRetry={() => fetchListings(currentPage, projectTypeFilter, maxPriceFilter)}
        />
      )}

      <ListingList
        listings={listings}
        projectTypeFilter={projectTypeFilter}
        maxPriceFilter={maxPriceFilter}
        onProjectTypeChange={(type) => {
          setProjectTypeFilter(type);
          setCurrentPage(0);
        }}
        onMaxPriceChange={(price) => {
          setMaxPriceFilter(price);
          setCurrentPage(0);
        }}
        currentPage={currentPage}
        totalPages={totalPages}
        totalElements={totalElements}
        pageSize={9}
        onPageChange={(page) => setCurrentPage(page)}
        loading={loading}
      />
    </div>
  );
}

export default MarketplacePage;
