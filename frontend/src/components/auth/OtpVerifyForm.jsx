import { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import ErrorMessage from '../common/ErrorMessage';
import LoadingSpinner from '../common/LoadingSpinner';
import styles from './OtpVerifyForm.module.css';

function OtpVerifyForm({
  onSubmit,
  onResend,
  email,
  loading = false,
  resending = false,
  error = null,
  initialExpiresInMinutes = 10,
}) {
  const [secondsRemaining, setSecondsRemaining] = useState(initialExpiresInMinutes * 60);

  const {
    register,
    handleSubmit,
    setValue,
    formState: { errors },
  } = useForm({
    defaultValues: {
      code: '',
    },
  });

  // Active countdown timer effect
  useEffect(() => {
    if (secondsRemaining <= 0) return;

    const intervalId = setInterval(() => {
      setSecondsRemaining((prev) => prev - 1);
    }, 1000);

    return () => clearInterval(intervalId);
  }, [secondsRemaining]);

  const isExpired = secondsRemaining <= 0;

  // Format seconds to MM:SS
  const formatTime = (totalSec) => {
    const mins = Math.floor(totalSec / 60);
    const secs = totalSec % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  };

  const handleResendClick = async () => {
    if (onResend) {
      await onResend();
      setSecondsRemaining(initialExpiresInMinutes * 60);
      setValue('code', '');
    }
  };

  return (
    <form className={styles.form} onSubmit={handleSubmit(onSubmit)} noValidate>
      {email && (
        <div className={styles.emailBanner}>
          Verification code sent to <strong>{email}</strong>
        </div>
      )}

      {error && <ErrorMessage title="Verification Failed" message={error} />}

      <div className={styles.fieldGroup}>
        <label htmlFor="otp-code" className={styles.label}>
          Enter 6-Digit Code
        </label>
        <input
          id="otp-code"
          type="text"
          maxLength={6}
          className={`${styles.otpInput} ${errors.code ? styles.inputError : ''}`}
          placeholder="482913"
          disabled={isExpired || loading}
          {...register('code', {
            required: 'Please enter the 6-digit verification code',
            pattern: {
              value: /^\d{6}$/,
              message: 'OTP code must be exactly 6 digits',
            },
          })}
        />
        {errors.code && <span className={styles.errorText}>{errors.code.message}</span>}
      </div>

      <div className={styles.timerContainer}>
        {!isExpired ? (
          <span className={styles.timerActive}>
            Code expires in: <span className={styles.timerCountdown}>{formatTime(secondsRemaining)}</span>
          </span>
        ) : (
          <span className={styles.timerExpired}>
            Code expired. Please request a new OTP code below.
          </span>
        )}
      </div>

      <button
        type="submit"
        className={`btn-primary ${styles.submitBtn}`}
        disabled={loading || isExpired}
      >
        {loading ? <LoadingSpinner message="" /> : 'Verify & Sign In'}
      </button>

      <div className={styles.resendRow}>
        <span>Didn't receive the code?</span>
        <button
          type="button"
          className={styles.resendBtn}
          onClick={handleResendClick}
          disabled={resending || loading}
        >
          {resending ? 'Sending...' : 'Resend OTP'}
        </button>
      </div>
    </form>
  );
}

export default OtpVerifyForm;
