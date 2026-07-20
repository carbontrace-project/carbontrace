import Pagination from '../common/Pagination';
import LoadingSpinner from '../common/LoadingSpinner';
import EmptyState from '../common/EmptyState';
import styles from './VendorList.module.css';

function VendorList({
  vendors = [],
  currentPage = 0,
  totalPages = 1,
  totalElements = 0,
  pageSize = 10,
  onPageChange,
  onSearchChange,
  searchQuery = '',
  onEdit,
  onToggleActive,
  isAdmin = false,
  loading = false,
}) {
  return (
    <div className={styles.container}>
      <div className={styles.toolbar}>
        <div className={styles.searchBox}>
          <svg className={styles.searchIcon} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <circle cx="11" cy="11" r="8" />
            <line x1="21" y1="21" x2="16.65" y2="16.65" />
          </svg>
          <input
            type="text"
            className={styles.searchInput}
            placeholder="Search vendors by name or email..."
            value={searchQuery}
            onChange={(e) => onSearchChange(e.target.value)}
          />
        </div>
      </div>

      {loading ? (
        <LoadingSpinner message="Loading vendors..." />
      ) : vendors.length === 0 ? (
        <EmptyState
          title="No vendors found"
          description={
            searchQuery
              ? `No vendors match your search "${searchQuery}".`
              : 'No logistics vendors have been registered yet.'
          }
        />
      ) : (
        <div className={styles.tableWrapper}>
          <table className={styles.table}>
            <thead>
              <tr>
                <th>ID</th>
                <th>Vendor Name</th>
                <th>Contact Email</th>
                <th>Country</th>
                <th>Status</th>
                {isAdmin && <th>Actions</th>}
              </tr>
            </thead>
            <tbody>
              {vendors.map((vendor) => (
                <tr key={vendor.id}>
                  <td>#{vendor.id}</td>
                  <td>
                    <strong>{vendor.name}</strong>
                  </td>
                  <td>{vendor.contactEmail}</td>
                  <td>{vendor.country}</td>
                  <td>
                    <span
                      className={`${styles.badge} ${
                        vendor.isActive ? styles.badgeActive : styles.badgeInactive
                      }`}
                    >
                      {vendor.isActive ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  {isAdmin && (
                    <td>
                      <div className={styles.actions}>
                        <button
                          type="button"
                          className={`btn-secondary ${styles.actionBtn}`}
                          onClick={() => onEdit && onEdit(vendor)}
                        >
                          Edit
                        </button>
                        <button
                          type="button"
                          className={`btn-secondary ${styles.actionBtn}`}
                          onClick={() => onToggleActive && onToggleActive(vendor)}
                        >
                          {vendor.isActive ? 'Deactivate' : 'Activate'}
                        </button>
                      </div>
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

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

export default VendorList;
