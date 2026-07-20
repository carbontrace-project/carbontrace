import UploadDropzone from '../components/shipment/UploadDropzone';
import styles from './UploadShipmentPage.module.css';

function UploadShipmentPage() {
  return (
    <div className={styles.container}>
      <header className={styles.header}>
        <h1 className={styles.title}>Upload Freight Bill</h1>
        <p className={styles.subtitle}>
          Upload ocean, air, road, or rail shipping invoices directly to Amazon S3 for AI document intelligence parsing.
        </p>
      </header>
      <UploadDropzone />
    </div>
  );
}

export default UploadShipmentPage;
