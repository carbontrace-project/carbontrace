import ShipmentCard from './ShipmentCard';
import Pagination from '../common/Pagination';
import LoadingSpinner from '../common/LoadingSpinner';
import EmptyState from '../common/EmptyState';
import styles from './ShipmentList.module.css';

function ShipmentList({
  shipments = [],
  vendors = [],
  statusFilter = '',
  vendorFilter = '',
  onStatusFilterChange,
  onVendorFilterChange,
  currentPage = 0,
  totalPages = 1,
  totalElements = 0,
  pageSize = 10,
  onPageChange,
  loading = false,
}) {
  const statuses = [
    { label: 'All Statuses', value: '' },
    { label: 'Uploaded', value: 'UPLOADED' },
    { label: 'Needs Review', value: 'NEEDS_REVIEW' },
    { label: 'Reviewed', value: 'REVIEWED' },
    { label: 'Calculated', value: 'CALCULATED' },
    { label: 'Failed', value: 'FAILED' },
  ];

  return (
    <div className={styles.container}>
      <div className={styles.filters}>
        <div className={styles.filterGroup}>
          <label htmlFor="filter-status" className={styles.filterLabel}>
            Status:
          </label>
          <select
            id="filter-status"
            className={styles.select}
            value={statusFilter}
            onChange={(e) => onStatusFilterChange(e.target.value)}
          >
            {statuses.map((s) => (
              <option key={s.value} value={s.value}>
                {s.label}
              </option>
            ))}
          </select>
        </div>

        <div className={styles.filterGroup}>
          <label htmlFor="filter-vendor" className={styles.filterLabel}>
            Vendor:
          </label>
          <select
            id="filter-vendor"
            className={styles.select}
            value={vendorFilter}
            onChange={(e) => onVendorFilterChange(e.target.value)}
          >
            <option value="">All Vendors</option>
            {vendors.map((v) => (
              <option key={v.id} value={v.id}>
                {v.name}
              </option>
            ))}
          </select>
        </div>
      </div>

      {loading ? (
        <LoadingSpinner message="Loading freight shipments..." />
      ) : shipments.length === 0 ? (
        <EmptyState
          title="No shipments found"
          description="There are no freight bills matching your selected filters."
        />
      ) : (
        <div className={styles.list}>
          {shipments.map((s) => (
            <ShipmentCard key={s.id} shipment={s} />
          ))}
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

export default ShipmentList;
