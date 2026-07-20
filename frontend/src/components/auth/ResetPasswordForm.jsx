import { useForm } from 'react-hook-form';
import ErrorMessage from '../common/ErrorMessage';
import LoadingSpinner from '../common/LoadingSpinner';
import styles from './ResetPasswordForm.module.css';

function ResetPasswordForm({ onSubmit, email, loading = false, error = null }) {
  const {
    register,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm({
    defaultValues: {
      code: '',
      newPassword: '',
      confirmPassword: '',
    },
  });

  const newPasswordValue = watch('newPassword');

  return (
    <form className={styles.form} onSubmit={handleSubmit(onSubmit)} noValidate>
      {email && (
        <div className={styles.emailBanner}>
          Reset code sent to <strong>{email}</strong>
        </div>
      )}

      {error && <ErrorMessage title="Reset Failed" message={error} />}

      <div className={styles.fieldGroup}>
        <label htmlFor="reset-code" className={styles.label}>
          6-Digit OTP Code
        </label>
        <input
          id="reset-code"
          type="text"
          maxLength={6}
          className={`${styles.input} ${styles.otpInput} ${errors.code ? styles.inputError : ''}`}
          placeholder="482913"
          {...register('code', {
            required: 'Please enter the 6-digit OTP code',
            pattern: {
              value: /^\d{6}$/,
              message: 'OTP code must be exactly 6 digits',
            },
          })}
        />
        {errors.code && <span className={styles.errorText}>{errors.code.message}</span>}
      </div>

      <div className={styles.fieldGroup}>
        <label htmlFor="reset-newPassword" className={styles.label}>
          New Password
        </label>
        <input
          id="reset-newPassword"
          type="password"
          className={`${styles.input} ${errors.newPassword ? styles.inputError : ''}`}
          placeholder="••••••••"
          {...register('newPassword', {
            required: 'New password is required',
            minLength: {
              value: 8,
              message: 'Password must be at least 8 characters long',
            },
            pattern: {
              value: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/,
              message: 'Must contain at least 1 uppercase letter, 1 lowercase letter, and 1 digit',
            },
          })}
        />
        {errors.newPassword && <span className={styles.errorText}>{errors.newPassword.message}</span>}
      </div>

      <div className={styles.fieldGroup}>
        <label htmlFor="reset-confirmPassword" className={styles.label}>
          Confirm New Password
        </label>
        <input
          id="reset-confirmPassword"
          type="password"
          className={`${styles.input} ${errors.confirmPassword ? styles.inputError : ''}`}
          placeholder="••••••••"
          {...register('confirmPassword', {
            required: 'Please confirm your new password',
            validate: (val) => val === newPasswordValue || 'Passwords do not match',
          })}
        />
        {errors.confirmPassword && (
          <span className={styles.errorText}>{errors.confirmPassword.message}</span>
        )}
      </div>

      <button type="submit" className={`btn-primary ${styles.submitBtn}`} disabled={loading}>
        {loading ? <LoadingSpinner message="" /> : 'Reset Password'}
      </button>
    </form>
  );
}

export default ResetPasswordForm;
