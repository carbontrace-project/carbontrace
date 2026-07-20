import { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../context/AuthContext';
import apiClient from '../api/axiosConfig';
import VendorList from '../components/vendor/VendorList';
import VendorForm from '../components/vendor/VendorForm';
import ErrorMessage from '../components/common/ErrorMessage';
import styles from './VendorsPage.module.css';

function VendorsPage() {
  const { role } = useAuth();
  const isAdmin = role === 'ROLE_ADMIN';

  const [vendors, setVendors] = useState([]);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [searchQuery, setSearchQuery] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingVendor, setEditingVendor] = useState(null);
  const [formLoading, setFormLoading] = useState(false);

  const fetchVendors = useCallback(async (page = 0, search = '') => {
    setLoading(true);
    setError(null);

    try {
      const response = await apiClient.get('/api/vendors', {
        params: {
          page,
          size: 10,
          search: search || undefined,
        },
      });

      const responseData = response.data?.data || response.data;
      const content = responseData?.content || responseData || [];
      const pageInfo = responseData?.page !== undefined ? responseData : { page: 0, totalPages: 1, totalElements: content.length };

      setVendors(content);
      setCurrentPage(pageInfo.page || 0);
      setTotalPages(pageInfo.totalPages || 1);
      setTotalElements(pageInfo.totalElements || content.length);
    } catch (err) {
      const message = err.response?.data?.message || err.message || 'Failed to fetch logistics vendors.';
      setError(message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchVendors(currentPage, searchQuery);
  }, [fetchVendors, currentPage, searchQuery]);

  const handleSearchChange = (query) => {
    setSearchQuery(query);
    setCurrentPage(0);
  };

  const handlePageChange = (newPage) => {
    setCurrentPage(newPage);
  };

  const handleOpenAddModal = () => {
    setEditingVendor(null);
    setIsModalOpen(true);
  };

  const handleOpenEditModal = (vendor) => {
    setEditingVendor(vendor);
    setIsModalOpen(true);
  };

  const handleModalClose = () => {
    setIsModalOpen(false);
    setEditingVendor(null);
  };

  const handleFormSubmit = async (formData) => {
    setFormLoading(true);

    try {
      if (editingVendor?.id) {
        await apiClient.put(`/api/vendors/${editingVendor.id}`, formData);
      } else {
        await apiClient.post('/api/vendors', formData);
      }

      setIsModalOpen(false);
      setEditingVendor(null);
      fetchVendors(currentPage, searchQuery);
    } catch (err) {
      alert(err.response?.data?.message || err.message || 'Failed to save vendor.');
    } finally {
      setFormLoading(false);
    }
  };

  const handleToggleActive = async (vendor) => {
    if (!isAdmin) return;

    try {
      await apiClient.put(`/api/vendors/${vendor.id}/toggle-active`);
      fetchVendors(currentPage, searchQuery);
    } catch (err) {
      alert(err.response?.data?.message || err.message || 'Failed to toggle vendor active state.');
    }
  };

  return (
    <div className={styles.container}>
      <header className={styles.header}>
        <div className={styles.titleGroup}>
          <h1 className={styles.title}>Logistics Vendors</h1>
          <p className={styles.subtitle}>
            Manage shipping, ocean, road, rail, and air freight logistics partners
          </p>
        </div>
        <button type="button" className="btn-primary" onClick={handleOpenAddModal}>
          + Add Vendor
        </button>
      </header>

      {error && <ErrorMessage title="Vendor Fetch Error" message={error} onRetry={() => fetchVendors(currentPage, searchQuery)} />}

      <VendorList
        vendors={vendors}
        currentPage={currentPage}
        totalPages={totalPages}
        totalElements={totalElements}
        pageSize={10}
        onPageChange={handlePageChange}
        onSearchChange={handleSearchChange}
        searchQuery={searchQuery}
        onEdit={handleOpenEditModal}
        onToggleActive={handleToggleActive}
        isAdmin={isAdmin}
        loading={loading}
      />

      <VendorForm
        isOpen={isModalOpen}
        onClose={handleModalClose}
        onSubmit={handleFormSubmit}
        vendor={editingVendor}
        loading={formLoading}
      />
    </div>
  );
}

export default VendorsPage;
