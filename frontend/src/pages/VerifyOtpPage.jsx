import { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import apiClient from '../api/axiosConfig';
import OtpVerifyForm from '../components/auth/OtpVerifyForm';
import styles from './VerifyOtpPage.module.css';

function VerifyOtpPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const { login } = useAuth();

  const [email, setEmail] = useState(location.state?.email || '');
  const [purpose] = useState(location.state?.purpose || 'VERIFICATION');
  const [loading, setLoading] = useState(false);
  const [resending, setResending] = useState(false);
  const [error, setError] = useState(null);

  const handleVerifySubmit = async (data) => {
    if (!email) {
      setError('Please provide an email address');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const response = await apiClient.post('/api/auth/verify-otp', {
        email,
        code: data.code,
        purpose,
      });

      const responseData = response.data?.data || response.data;

      // Auto-login upon successful verification
      login(responseData);

      // Redirect to dashboard (home)
      navigate('/', { replace: true });
    } catch (err) {
      const message =
        err.response?.data?.message || err.message || 'OTP verification failed. Please check the code.';
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  const handleResendOtp = async () => {
    if (!email) {
      setError('Email address is missing');
      return;
    }

    setResending(true);
    setError(null);

    try {
      await apiClient.post('/api/auth/resend-otp', {
        email,
        purpose,
      });
    } catch (err) {
      const message = err.response?.data?.message || err.message || 'Failed to resend OTP code.';
      setError(message);
    } finally {
      setResending(false);
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
        <p className={styles.subtitle}>Verify your account email</p>

        {!location.state?.email && (
          <div className={styles.emailInputGroup}>
            <label htmlFor="manual-email" className={styles.label}>
              Verification Email Address
            </label>
            <input
              id="manual-email"
              type="email"
              className={styles.input}
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="auditor@acme.com"
            />
          </div>
        )}

        <OtpVerifyForm
          onSubmit={handleVerifySubmit}
          onResend={handleResendOtp}
          email={email}
          loading={loading}
          resending={resending}
          error={error}
        />
      </div>
    </div>
  );
}

export default VerifyOtpPage;
