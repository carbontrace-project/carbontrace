import { formatEmissions } from '../../utils/formatters';
import styles from './VendorEmissionsTable.module.css';

function VendorEmissionsTable({ vendorData = [] }) {
  if (!vendorData || vendorData.length === 0) {
    return (
      <div className={styles.card}>
        <h3 className={styles.title}>Logistics Vendor Footprint Breakdown</h3>
        <p className={styles.emptyText}>
          No vendor emissions recorded yet.
        </p>
      </div>
    );
  }

  return (
    <div className={styles.card}>
      <h3 className={styles.title}>Logistics Vendor Footprint Breakdown</h3>
      <div className={styles.tableWrapper}>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>Vendor Name</th>
              <th>Shipments</th>
              <th>Calculated Emissions</th>
            </tr>
          </thead>
          <tbody>
            {vendorData.map((v) => (
              <tr key={v.vendorId || v.vendorName}>
                <td>
                  <strong>{v.vendorName}</strong>
                </td>
                <td>{v.shipmentCount || 0}</td>
                <td>
                  <strong>{formatEmissions(v.totalEmissionsKgco2e)}</strong>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

export default VendorEmissionsTable;
