import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link } from 'react-router-dom';
import apiClient from '../api/axiosConfig';
import ResetPasswordForm from '../components/auth/ResetPasswordForm';
import ErrorMessage from '../components/common/ErrorMessage';
import LoadingSpinner from '../components/common/LoadingSpinner';
import styles from './ForgotPasswordPage.module.css';

function ForgotPasswordPage() {
  const [step, setStep] = useState(1); // Step 1: Request OTP, Step 2: Reset Password, Step 3: Done
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    defaultValues: {
      email: '',
    },
  });

  // Step 1 Submission: Request OTP
  const handleRequestOtpSubmit = async (data) => {
    setLoading(true);
    setError(null);

    try {
      await apiClient.post('/api/auth/forgot-password', {
        email: data.email,
      });

      setEmail(data.email);
      setStep(2);
    } catch (err) {
      const message =
        err.response?.data?.message || err.message || 'Failed to send password reset OTP.';
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  // Step 2 Submission: Reset Password
  const handleResetSubmit = async (data) => {
    setLoading(true);
    setError(null);

    try {
      await apiClient.post('/api/auth/reset-password', {
        email,
        code: data.code,
        newPassword: data.newPassword,
      });

      setStep(3);
    } catch (err) {
      const message =
        err.response?.data?.message || err.message || 'Password reset failed. Please check the OTP code.';
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.container}>
      <div className={styles.card}>
        <div className={styles.brandHeader}>
          <svg className={styles.logoIcon} viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 17.93c-3.95-.49-7-3.85-7-7.93 0-.62.08-1.21.21-1.79L9 15v1c0 1.1.9 2 2 2v1.93zm6.9-2.54c-.26-.81-1-1.39-1.9-1.39h-1v-3c0-.55-.45-1-1-1H8v-2h2c.55 0 1-.45 1-1V7h2c1.1 0 2-.9 2-2v-.41c2.93 1.19 5 4.06 5 7.41 0 2.08-.8 3.97-2.1 5.39z" />
          </svg>
          <h2 className={styles.brandTitle}>CarbonTrace</h2>
        </div>
        <p className={styles.subtitle}>
          {step === 1 && 'Reset your password'}
          {step === 2 && 'Enter OTP code & new password'}
          {step === 3 && 'Password Reset Complete'}
        </p>

        {step === 1 && (
          <form className={styles.form} onSubmit={handleSubmit(handleRequestOtpSubmit)} noValidate>
            {error && <ErrorMessage title="Request Failed" message={error} />}

            <div className={styles.fieldGroup}>
              <label htmlFor="forgot-email" className={styles.label}>
                Registered Email Address
              </label>
              <input
                id="forgot-email"
                type="email"
                className={`${styles.input} ${errors.email ? styles.inputError : ''}`}
                placeholder="auditor@acme.com"
                {...register('email', {
                  required: 'Email is required',
                  pattern: {
                    value: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
                    message: 'Please enter a valid email address',
                  },
                })}
              />
              {errors.email && <span className={styles.errorText}>{errors.email.message}</span>}
            </div>

            <button type="submit" className={`btn-primary ${styles.submitBtn}`} disabled={loading}>
              {loading ? <LoadingSpinner message="" /> : 'Send Reset Code'}
            </button>

            <p className={styles.footerText}>
              Remembered your password?{' '}
              <Link to="/login" className={styles.footerLink}>
                Back to Sign In
              </Link>
            </p>
          </form>
        )}

        {step === 2 && (
          <ResetPasswordForm
            onSubmit={handleResetSubmit}
            email={email}
            loading={loading}
            error={error}
          />
        )}

        {step === 3 && (
          <div className={styles.successBanner}>
            <h3 className={styles.successTitle}>Password Updated!</h3>
            <p className={styles.successText}>
              Your password has been successfully reset. You may now log in with your new credentials.
            </p>
            <Link to="/login" className={`btn-primary ${styles.successLink}`}>
              Sign In Now
            </Link>
          </div>
        )}
      </div>
    </div>
  );
}

export default ForgotPasswordPage;
