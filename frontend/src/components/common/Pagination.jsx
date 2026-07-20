import styles from './Pagination.module.css';

function Pagination({
  currentPage = 0,
  totalPages = 1,
  onPageChange,
  totalElements,
  pageSize = 10,
}) {
  if (totalPages <= 1 && totalElements === undefined) {
    return null;
  }

  const isFirst = currentPage <= 0;
  const isLast = currentPage >= totalPages - 1;

  const startElement = currentPage * pageSize + 1;
  const endElement = totalElements ? Math.min((currentPage + 1) * pageSize, totalElements) : null;

  return (
    <div className={styles.container}>
      <div className={styles.info}>
        {totalElements !== undefined ? (
          <span>
            Showing <strong>{totalElements > 0 ? startElement : 0}</strong>-<strong>{endElement}</strong> of{' '}
            <strong>{totalElements}</strong> items
          </span>
        ) : (
          <span>
            Page <strong>{currentPage + 1}</strong> of <strong>{totalPages}</strong>
          </span>
        )}
      </div>

      <div className={styles.controls}>
        <button
          type="button"
          className={styles.pageBtn}
          onClick={() => onPageChange(currentPage - 1)}
          disabled={isFirst}
          aria-label="Previous Page"
        >
          &larr; Prev
        </button>

        <span className={styles.pageIndicator}>
          {currentPage + 1} / {Math.max(totalPages, 1)}
        </span>

        <button
          type="button"
          className={styles.pageBtn}
          onClick={() => onPageChange(currentPage + 1)}
          disabled={isLast}
          aria-label="Next Page"
        >
          Next &rarr;
        </button>
      </div>
    </div>
  );
}

export default Pagination;
