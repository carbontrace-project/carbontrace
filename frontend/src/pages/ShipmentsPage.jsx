import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import apiClient from '../api/axiosConfig';
import ShipmentList from '../components/shipment/ShipmentList';
import ErrorMessage from '../components/common/ErrorMessage';
import styles from './ShipmentsPage.module.css';

function ShipmentsPage() {
  const navigate = useNavigate();

  const [shipments, setShipments] = useState([]);
  const [vendors, setVendors] = useState([]);
  const [statusFilter, setStatusFilter] = useState('');
  const [vendorFilter, setVendorFilter] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Fetch vendors list for dropdown filter
  useEffect(() => {
    const fetchVendors = async () => {
      try {
        const res = await apiClient.get('/api/vendors?page=0&size=100');
        const data = res.data?.data?.content || res.data?.content || [];
        setVendors(data);
      } catch {
        // Silent catch for filter dropdown
      }
    };
    fetchVendors();
  }, []);

  const fetchShipments = useCallback(async (page = 0, status = '', vendorId = '') => {
    setLoading(true);
    setError(null);

    try {
      const response = await apiClient.get('/api/shipments', {
        params: {
          page,
          size: 10,
          status: status || undefined,
          vendorId: vendorId || undefined,
        },
      });

      const resData = response.data?.data || response.data;
      const content = resData?.content || resData || [];
      const pageInfo = resData?.page !== undefined ? resData : { page: 0, totalPages: 1, totalElements: content.length };

      setShipments(content);
      setCurrentPage(pageInfo.page || 0);
      setTotalPages(pageInfo.totalPages || 1);
      setTotalElements(pageInfo.totalElements || content.length);
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Failed to fetch shipments.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchShipments(currentPage, statusFilter, vendorFilter);
  }, [fetchShipments, currentPage, statusFilter, vendorFilter]);

  const handleStatusFilterChange = (status) => {
    setStatusFilter(status);
    setCurrentPage(0);
  };

  const handleVendorFilterChange = (vendorId) => {
    setVendorFilter(vendorId);
    setCurrentPage(0);
  };

  return (
    <div className={styles.container}>
      <header className={styles.header}>
        <div className={styles.titleGroup}>
          <h1 className={styles.title}>Freight Shipments</h1>
          <p className={styles.subtitle}>
            View and audit extracted shipping bills, review AI fields, calculate carbon emissions, and purchase offsets
          </p>
        </div>
        <button type="button" className="btn-primary" onClick={() => navigate('/upload')}>
          + Upload Freight Bill
        </button>
      </header>

      {error && (
        <ErrorMessage
          title="Shipments Fetch Error"
          message={error}
          onRetry={() => fetchShipments(currentPage, statusFilter, vendorFilter)}
        />
      )}

      <ShipmentList
        shipments={shipments}
        vendors={vendors}
        statusFilter={statusFilter}
        vendorFilter={vendorFilter}
        onStatusFilterChange={handleStatusFilterChange}
        onVendorFilterChange={handleVendorFilterChange}
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

export default ShipmentsPage;
