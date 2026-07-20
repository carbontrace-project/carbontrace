import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import LoadingSpinner from '../common/LoadingSpinner';
import styles from './GoalForm.module.css';

function GoalForm({ isOpen, onClose, onSubmit, goal = null, loading = false }) {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm();

  useEffect(() => {
    if (goal) {
      reset({
        title: goal.title || '',
        targetReductionPercentage: goal.targetReductionPercentage ?? 15,
        baselineEmissionsKgco2e: goal.baselineEmissionsKgco2e ?? '',
        startDate: goal.startDate || '',
        endDate: goal.endDate || '',
      });
    } else {
      reset({
        title: '',
        targetReductionPercentage: 15,
        baselineEmissionsKgco2e: '',
        startDate: new Date().toISOString().split('T')[0],
        endDate: '2030-12-31',
      });
    }
  }, [goal, reset, isOpen]);

  if (!isOpen) return null;

  const isEdit = Boolean(goal?.id);

  return (
    <div className={styles.overlay} onClick={onClose}>
      <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
        <div className={styles.header}>
          <h3 className={styles.title}>{isEdit ? 'Edit Reduction Goal' : 'Create Reduction Goal'}</h3>
          <button type="button" className={styles.closeBtn} onClick={onClose} aria-label="Close modal">
            &times;
          </button>
        </div>

        <form className={styles.form} onSubmit={handleSubmit(onSubmit)} noValidate>
          <div className={styles.fieldGroup}>
            <label className={styles.label}>Goal Title</label>
            <input
              type="text"
              className={styles.input}
              placeholder="e.g. 15% Maritime Reduction by 2030"
              {...register('title', { required: 'Goal title is required' })}
            />
            {errors.title && <span className={styles.errorText}>{errors.title.message}</span>}
          </div>

          <div className={styles.fieldGroup}>
            <label className={styles.label}>Target Reduction Percentage (%)</label>
            <input
              type="number"
              step="0.1"
              className={styles.input}
              {...register('targetReductionPercentage', {
                required: 'Target reduction percentage is required',
                min: { value: 1, message: 'Minimum 1%' },
                max: { value: 100, message: 'Maximum 100%' },
              })}
            />
            {errors.targetReductionPercentage && (
              <span className={styles.errorText}>{errors.targetReductionPercentage.message}</span>
            )}
          </div>

          <div className={styles.fieldGroup}>
            <label className={styles.label}>Baseline Emissions (kgCO₂e)</label>
            <input
              type="number"
              step="0.01"
              className={styles.input}
              placeholder="e.g. 50000"
              {...register('baselineEmissionsKgco2e', { required: 'Baseline emissions is required' })}
            />
            {errors.baselineEmissionsKgco2e && (
              <span className={styles.errorText}>{errors.baselineEmissionsKgco2e.message}</span>
            )}
          </div>

          <div className={styles.fieldGroup}>
            <label className={styles.label}>Start Date</label>
            <input type="date" className={styles.input} {...register('startDate', { required: 'Start date is required' })} />
            {errors.startDate && <span className={styles.errorText}>{errors.startDate.message}</span>}
          </div>

          <div className={styles.fieldGroup}>
            <label className={styles.label}>End Date</label>
            <input type="date" className={styles.input} {...register('endDate', { required: 'End date is required' })} />
            {errors.endDate && <span className={styles.errorText}>{errors.endDate.message}</span>}
          </div>

          <div className={styles.footer}>
            <button type="button" className="btn-secondary" onClick={onClose} disabled={loading}>
              Cancel
            </button>
            <button type="submit" className="btn-primary" disabled={loading}>
              {loading ? <LoadingSpinner message="" /> : isEdit ? 'Update Goal' : 'Save Goal'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default GoalForm;
