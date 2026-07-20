import { useForm } from 'react-hook-form';
import { Link } from 'react-router-dom';
import ErrorMessage from '../common/ErrorMessage';
import LoadingSpinner from '../common/LoadingSpinner';
import styles from './LoginForm.module.css';

function LoginForm({ onSubmit, loading = false, error = null }) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    defaultValues: {
      email: '',
      password: '',
    },
  });

  return (
    <form className={styles.form} onSubmit={handleSubmit(onSubmit)} noValidate>
      {error && <ErrorMessage title="Login Failed" message={error} />}

      <div className={styles.fieldGroup}>
        <label htmlFor="login-email" className={styles.label}>
          Email Address
        </label>
        <input
          id="login-email"
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

      <div className={styles.fieldGroup}>
        <div className={styles.forgotLinkWrapper}>
          <label htmlFor="login-password" className={styles.label} style={{ marginRight: 'auto' }}>
            Password
          </label>
          <Link to="/forgot-password" className={styles.forgotLink}>
            Forgot password?
          </Link>
        </div>
        <input
          id="login-password"
          type="password"
          className={`${styles.input} ${errors.password ? styles.inputError : ''}`}
          placeholder="••••••••"
          {...register('password', {
            required: 'Password is required',
            minLength: {
              value: 8,
              message: 'Password must be at least 8 characters long',
            },
            pattern: {
              value: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/,
              message: 'Password must contain at least 1 uppercase letter, 1 lowercase letter, and 1 digit',
            },
          })}
        />
        {errors.password && <span className={styles.errorText}>{errors.password.message}</span>}
      </div>

      <button type="submit" className={`btn-primary ${styles.submitBtn}`} disabled={loading}>
        {loading ? <LoadingSpinner message="" /> : 'Sign In'}
      </button>

      <p className={styles.footerText}>
        Don't have an account?{' '}
        <Link to="/register" className={styles.footerLink}>
          Register here
        </Link>
      </p>
    </form>
  );
}

export default LoginForm;
