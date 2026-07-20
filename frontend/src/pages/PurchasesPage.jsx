import { useState, useEffect, useCallback } from 'react';
import apiClient from '../api/axiosConfig';
import PurchaseHistoryTable from '../components/purchases/PurchaseHistoryTable';
import ErrorMessage from '../components/common/ErrorMessage';
import styles from './PurchasesPage.module.css';

function PurchasesPage() {
  const [purchases, setPurchases] = useState([]);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchPurchases = useCallback(async (page = 0) => {
    setLoading(true);
    setError(null);

    try {
      const response = await apiClient.get('/api/purchases', {
        params: {
          page,
          size: 10,
        },
      });

      const resData = response.data?.data || response.data;
      const content = resData?.content || resData || [];
      const pageInfo = resData?.page !== undefined ? resData : { page: 0, totalPages: 1, totalElements: content.length };

      setPurchases(content);
      setCurrentPage(pageInfo.page || 0);
      setTotalPages(pageInfo.totalPages || 1);
      setTotalElements(pageInfo.totalElements || content.length);
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Failed to load purchase history.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchPurchases(currentPage);
  }, [fetchPurchases, currentPage]);

  return (
    <div className={styles.container}>
      <header className={styles.header}>
        <h1 className={styles.title}>Offset Purchase History</h1>
        <p className={styles.subtitle}>
          Audit trail of carbon credit transactions executed by the autonomous AI purchasing agent with decision logs.
        </p>
      </header>

      {error && <ErrorMessage title="Purchases Load Error" message={error} onRetry={() => fetchPurchases(currentPage)} />}

      <PurchaseHistoryTable
        purchases={purchases}
        currentPage={currentPage}
        totalPages={totalPages}
        totalElements={totalElements}
        pageSize={10}
        onPageChange={(page) => setCurrentPage(page)}
        loading={loading}
      />
    </div>
  );
}

export default PurchasesPage;
