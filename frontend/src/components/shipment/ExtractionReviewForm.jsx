import { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import apiClient from '../../api/axiosConfig';
import LoadingSpinner from '../common/LoadingSpinner';
import ErrorMessage from '../common/ErrorMessage';
import styles from './ExtractionReviewForm.module.css';

function ExtractionReviewForm({ shipment, onSaveSuccess }) {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [pdfLoading, setPdfLoading] = useState(false);

  const fieldConfidence = shipment.fieldConfidence || {};

  const { register, handleSubmit, reset } = useForm();

  useEffect(() => {
    if (shipment) {
      reset({
        invoiceNumber: shipment.invoiceNumber || '',
        carrierName: shipment.carrierName || '',
        shipmentDate: shipment.shipmentDate || '',
        originCity: shipment.originCity || '',
        originCountry: shipment.originCountry || '',
        originLat: shipment.originLat ?? '',
        originLng: shipment.originLng ?? '',
        destinationCity: shipment.destinationCity || '',
        destinationCountry: shipment.destinationCountry || '',
        destinationLat: shipment.destinationLat ?? '',
        destinationLng: shipment.destinationLng ?? '',
        transportMode: shipment.transportMode || 'SEA',
        fuelType: shipment.fuelType || 'HEAVY_FUEL_OIL',
        weightTonnes: shipment.weightTonnes ?? '',
        distanceKm: shipment.distanceKm ?? '',
      });
    }
  }, [shipment, reset]);

  const isLowConfidence = (fieldName) => {
    const level = fieldConfidence[fieldName];
    return level === 'LOW' || level === 'MEDIUM';
  };

  const getInputClass = (fieldName) => {
    const warning = isLowConfidence(fieldName);
    return `${styles.input} ${warning ? styles.confidenceWarning : ''}`;
  };

  const handleViewPdf = async () => {
    setPdfLoading(true);
    try {
      const response = await apiClient.get(`/api/shipments/${shipment.id}/document-url`);
      const data = response.data?.data || response.data;
      const url = data?.documentUrl || data?.url;
      if (url) {
        window.open(url, '_blank');
      } else {
        alert('Could not retrieve document URL.');
      }
    } catch {
      alert('Failed to get document URL.');
    } finally {
      setPdfLoading(false);
    }
  };

  const onSubmit = async (data) => {
    setLoading(true);
    setError(null);

    const payload = {
      invoiceNumber: data.invoiceNumber,
      carrierName: data.carrierName,
      shipmentDate: data.shipmentDate,
      originCity: data.originCity,
      originCountry: data.originCountry,
      originLat: data.originLat !== '' ? Number(data.originLat) : null,
      originLng: data.originLng !== '' ? Number(data.originLng) : null,
      destinationCity: data.destinationCity,
      destinationCountry: data.destinationCountry,
      destinationLat: data.destinationLat !== '' ? Number(data.destinationLat) : null,
      destinationLng: data.destinationLng !== '' ? Number(data.destinationLng) : null,
      transportMode: data.transportMode,
      fuelType: data.fuelType,
      weightTonnes: Number(data.weightTonnes),
      distanceKm: data.distanceKm !== '' && data.distanceKm !== null ? Number(data.distanceKm) : null,
    };

    try {
      const response = await apiClient.put(`/api/shipments/${shipment.id}/review`, payload);
      const updated = response.data?.data || response.data;
      if (onSaveSuccess) {
        onSaveSuccess(updated);
      }
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Failed to save review.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <form className={styles.form} onSubmit={handleSubmit(onSubmit)} noValidate>
      <div className={styles.pdfBanner}>
        <span className={styles.pdfText}>Original Document: {shipment.fileName || `Invoice #${shipment.id}.pdf`}</span>
        <button type="button" className="btn-secondary" onClick={handleViewPdf} disabled={pdfLoading}>
          {pdfLoading ? <LoadingSpinner message="" /> : 'View Original PDF ↗'}
        </button>
      </div>

      {error && <ErrorMessage title="Review Error" message={error} />}

      <h3 className={styles.sectionTitle}>1. Invoice Metadata</h3>
      <div className={styles.grid}>
        <div className={styles.fieldGroup}>
          <label className={styles.label}>
            Invoice Number
            {isLowConfidence('invoiceNumber') && (
              <span className={styles.confidenceBadge} title="AI extracted — please verify">
                Verify AI
              </span>
            )}
          </label>
          <input
            type="text"
            className={getInputClass('invoiceNumber')}
            title={isLowConfidence('invoiceNumber') ? 'AI extracted — please verify' : ''}
            {...register('invoiceNumber', { required: 'Invoice number is required' })}
          />
        </div>

        <div className={styles.fieldGroup}>
          <label className={styles.label}>
            Carrier Name
            {isLowConfidence('carrierName') && (
              <span className={styles.confidenceBadge} title="AI extracted — please verify">
                Verify AI
              </span>
            )}
          </label>
          <input
            type="text"
            className={getInputClass('carrierName')}
            title={isLowConfidence('carrierName') ? 'AI extracted — please verify' : ''}
            {...register('carrierName', { required: 'Carrier name is required' })}
          />
        </div>

        <div className={styles.fieldGroup}>
          <label className={styles.label}>
            Shipment Date
            {isLowConfidence('shipmentDate') && (
              <span className={styles.confidenceBadge} title="AI extracted — please verify">
                Verify AI
              </span>
            )}
          </label>
          <input
            type="date"
            className={getInputClass('shipmentDate')}
            title={isLowConfidence('shipmentDate') ? 'AI extracted — please verify' : ''}
            {...register('shipmentDate', { required: 'Shipment date is required' })}
          />
        </div>
      </div>

      <h3 className={styles.sectionTitle}>2. Transport Mode & Cargo Weight</h3>
      <div className={styles.grid}>
        <div className={styles.fieldGroup}>
          <label className={styles.label}>Transport Mode</label>
          <select className={styles.input} {...register('transportMode')}>
            <option value="ROAD">ROAD</option>
            <option value="RAIL">RAIL</option>
            <option value="SEA">SEA</option>
            <option value="AIR">AIR</option>
          </select>
        </div>

        <div className={styles.fieldGroup}>
          <label className={styles.label}>Fuel Type</label>
          <select className={styles.input} {...register('fuelType')}>
            <option value="HEAVY_FUEL_OIL">HEAVY_FUEL_OIL</option>
            <option value="DIESEL">DIESEL</option>
            <option value="JET_FUEL">JET_FUEL</option>
            <option value="ELECTRIC">ELECTRIC</option>
            <option value="ANY">ANY</option>
          </select>
        </div>

        <div className={styles.fieldGroup}>
          <label className={styles.label}>
            Weight (Tonnes)
            {isLowConfidence('weightTonnes') && (
              <span className={styles.confidenceBadge} title="AI extracted — please verify">
                Verify AI
              </span>
            )}
          </label>
          <input
            type="number"
            step="0.001"
            className={getInputClass('weightTonnes')}
            title={isLowConfidence('weightTonnes') ? 'AI extracted — please verify' : ''}
            {...register('weightTonnes', { required: 'Weight in tonnes is required' })}
          />
        </div>

        <div className={styles.fieldGroup}>
          <label className={styles.label}>Distance (Km, Optional)</label>
          <input
            type="number"
            step="0.01"
            className={styles.input}
            placeholder="Leave empty to compute from coordinates"
            {...register('distanceKm')}
          />
        </div>
      </div>

      <h3 className={styles.sectionTitle}>3. Origin Location</h3>
      <div className={styles.grid}>
        <div className={styles.fieldGroup}>
          <label className={styles.label}>Origin City</label>
          <input type="text" className={styles.input} {...register('originCity')} />
        </div>
        <div className={styles.fieldGroup}>
          <label className={styles.label}>Origin Country</label>
          <input type="text" className={styles.input} {...register('originCountry')} />
        </div>
        <div className={styles.fieldGroup}>
          <label className={styles.label}>Origin Lat</label>
          <input type="number" step="0.0001" className={styles.input} {...register('originLat')} />
        </div>
        <div className={styles.fieldGroup}>
          <label className={styles.label}>Origin Lng</label>
          <input type="number" step="0.0001" className={styles.input} {...register('originLng')} />
        </div>
      </div>

      <h3 className={styles.sectionTitle}>4. Destination Location</h3>
      <div className={styles.grid}>
        <div className={styles.fieldGroup}>
          <label className={styles.label}>Destination City</label>
          <input type="text" className={styles.input} {...register('destinationCity')} />
        </div>
        <div className={styles.fieldGroup}>
          <label className={styles.label}>Destination Country</label>
          <input type="text" className={styles.input} {...register('destinationCountry')} />
        </div>
        <div className={styles.fieldGroup}>
          <label className={styles.label}>Destination Lat</label>
          <input type="number" step="0.0001" className={styles.input} {...register('destinationLat')} />
        </div>
        <div className={styles.fieldGroup}>
          <label className={styles.label}>Destination Lng</label>
          <input type="number" step="0.0001" className={styles.input} {...register('destinationLng')} />
        </div>
      </div>

      <div className={styles.submitBar}>
        <button type="submit" className="btn-primary" disabled={loading}>
          {loading ? <LoadingSpinner message="" /> : 'Save & Confirm Extraction Review'}
        </button>
      </div>
    </form>
  );
}

export default ExtractionReviewForm;
