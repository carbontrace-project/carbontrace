import ListingCard from './ListingCard';
import Pagination from '../common/Pagination';
import LoadingSpinner from '../common/LoadingSpinner';
import EmptyState from '../common/EmptyState';
import styles from './ListingList.module.css';

function ListingList({
  listings = [],
  projectTypeFilter = '',
  maxPriceFilter = '',
  onProjectTypeChange,
  onMaxPriceChange,
  currentPage = 0,
  totalPages = 1,
  totalElements = 0,
  pageSize = 10,
  onPageChange,
  loading = false,
}) {
  const projectTypes = [
    { label: 'All Project Types', value: '' },
    { label: 'Reforestation', value: 'REFORESTATION' },
    { label: 'Renewable Energy', value: 'RENEWABLE_ENERGY' },
    { label: 'Methane Capture', value: 'METHANE_CAPTURE' },
    { label: 'Soil Carbon', value: 'SOIL_CARBON' },
    { label: 'Direct Air Capture', value: 'DIRECT_AIR_CAPTURE' },
  ];

  return (
    <div className={styles.container}>
      <div className={styles.demoBanner}>
        Simulated marketplace — demo data
      </div>

      <div className={styles.filters}>
        <div className={styles.filterGroup}>
          <label htmlFor="filter-project-type" className={styles.filterLabel}>
            Project Type:
          </label>
          <select
            id="filter-project-type"
            className={styles.select}
            value={projectTypeFilter}
            onChange={(e) => onProjectTypeChange(e.target.value)}
          >
            {projectTypes.map((t) => (
              <option key={t.value} value={t.value}>
                {t.label}
              </option>
            ))}
          </select>
        </div>

        <div className={styles.filterGroup}>
          <label htmlFor="filter-max-price" className={styles.filterLabel}>
            Max Price ($/tonne):
          </label>
          <input
            id="filter-max-price"
            type="number"
            className={styles.input}
            placeholder="e.g. 50"
            value={maxPriceFilter}
            onChange={(e) => onMaxPriceChange(e.target.value)}
          />
        </div>
      </div>

      {loading ? (
        <LoadingSpinner message="Loading carbon credit listings..." />
      ) : listings.length === 0 ? (
        <EmptyState
          title="No carbon offset listings found"
          description="No available credit listings match your selected price or project filters."
        />
      ) : (
        <div className={styles.grid}>
          {listings.map((l) => (
            <ListingCard key={l.id} listing={l} />
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

export default ListingList;
