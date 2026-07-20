import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import LoadingSpinner from '../common/LoadingSpinner';
import styles from './VendorForm.module.css';

function VendorForm({ isOpen, onClose, onSubmit, vendor = null, loading = false }) {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm({
    defaultValues: {
      name: '',
      contactEmail: '',
      country: '',
    },
  });

  useEffect(() => {
    if (vendor) {
      reset({
        name: vendor.name || '',
        contactEmail: vendor.contactEmail || '',
        country: vendor.country || '',
      });
    } else {
      reset({
        name: '',
        contactEmail: '',
        country: '',
      });
    }
  }, [vendor, reset, isOpen]);

  if (!isOpen) return null;

  const isEdit = Boolean(vendor?.id);

  return (
    <div className={styles.overlay} onClick={onClose}>
      <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
        <div className={styles.header}>
          <h3 className={styles.title}>{isEdit ? 'Edit Logistics Vendor' : 'Add Logistics Vendor'}</h3>
          <button type="button" className={styles.closeBtn} onClick={onClose} aria-label="Close modal">
            &times;
          </button>
        </div>

        <form className={styles.form} onSubmit={handleSubmit(onSubmit)} noValidate>
          <div className={styles.fieldGroup}>
            <label htmlFor="vendor-name" className={styles.label}>
              Vendor Name
            </label>
            <input
              id="vendor-name"
              type="text"
              className={`${styles.input} ${errors.name ? styles.inputError : ''}`}
              placeholder="e.g. OceanBridge Freight"
              {...register('name', { required: 'Vendor name is required' })}
            />
            {errors.name && <span className={styles.errorText}>{errors.name.message}</span>}
          </div>

          <div className={styles.fieldGroup}>
            <label htmlFor="vendor-contactEmail" className={styles.label}>
              Contact Email
            </label>
            <input
              id="vendor-contactEmail"
              type="email"
              className={`${styles.input} ${errors.contactEmail ? styles.inputError : ''}`}
              placeholder="ops@oceanbridge.com"
              {...register('contactEmail', {
                required: 'Contact email is required',
                pattern: {
                  value: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
                  message: 'Please enter a valid email address',
                },
              })}
            />
            {errors.contactEmail && (
              <span className={styles.errorText}>{errors.contactEmail.message}</span>
            )}
          </div>

          <div className={styles.fieldGroup}>
            <label htmlFor="vendor-country" className={styles.label}>
              Country
            </label>
            <input
              id="vendor-country"
              type="text"
              className={`${styles.input} ${errors.country ? styles.inputError : ''}`}
              placeholder="e.g. Germany, USA, India, China"
              {...register('country', { required: 'Country is required' })}
            />
            {errors.country && <span className={styles.errorText}>{errors.country.message}</span>}
          </div>

          <div className={styles.footer}>
            <button type="button" className="btn-secondary" onClick={onClose} disabled={loading}>
              Cancel
            </button>
            <button type="submit" className="btn-primary" disabled={loading}>
              {loading ? <LoadingSpinner message="" /> : isEdit ? 'Update Vendor' : 'Add Vendor'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default VendorForm;
