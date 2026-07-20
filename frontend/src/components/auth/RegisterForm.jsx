import { useForm } from 'react-hook-form';
import { Link } from 'react-router-dom';
import ErrorMessage from '../common/ErrorMessage';
import LoadingSpinner from '../common/LoadingSpinner';
import styles from './RegisterForm.module.css';

function RegisterForm({ onSubmit, loading = false, error = null }) {
  const {
    register,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm({
    defaultValues: {
      firstName: '',
      lastName: '',
      companyName: '',
      email: '',
      password: '',
      confirmPassword: '',
      role: 'ROLE_AUDITOR',
    },
  });

  const passwordValue = watch('password');
  const selectedRole = watch('role');

  return (
    <form className={styles.form} onSubmit={handleSubmit(onSubmit)} noValidate>
      {error && <ErrorMessage title="Registration Failed" message={error} />}

      <div className={styles.row}>
        <div className={styles.fieldGroup}>
          <label htmlFor="reg-firstName" className={styles.label}>
            First Name
          </label>
          <input
            id="reg-firstName"
            type="text"
            className={`${styles.input} ${errors.firstName ? styles.inputError : ''}`}
            placeholder="Ananya"
            {...register('firstName', { required: 'First name is required' })}
          />
          {errors.firstName && <span className={styles.errorText}>{errors.firstName.message}</span>}
        </div>

        <div className={styles.fieldGroup}>
          <label htmlFor="reg-lastName" className={styles.label}>
            Last Name
          </label>
          <input
            id="reg-lastName"
            type="text"
            className={`${styles.input} ${errors.lastName ? styles.inputError : ''}`}
            placeholder="Rao"
            {...register('lastName', { required: 'Last name is required' })}
          />
          {errors.lastName && <span className={styles.errorText}>{errors.lastName.message}</span>}
        </div>
      </div>

      <div className={styles.fieldGroup}>
        <label htmlFor="reg-companyName" className={styles.label}>
          Company Name
        </label>
        <input
          id="reg-companyName"
          type="text"
          className={`${styles.input} ${errors.companyName ? styles.inputError : ''}`}
          placeholder="Acme Global Logistics"
          {...register('companyName', { required: 'Company name is required' })}
        />
        {errors.companyName && <span className={styles.errorText}>{errors.companyName.message}</span>}
      </div>

      <div className={styles.fieldGroup}>
        <label htmlFor="reg-email" className={styles.label}>
          Company Email
        </label>
        <input
          id="reg-email"
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

      <div className={styles.row}>
        <div className={styles.fieldGroup}>
          <label htmlFor="reg-password" className={styles.label}>
            Password
          </label>
          <input
            id="reg-password"
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
                message: 'Must include 1 uppercase, 1 lowercase, and 1 digit',
              },
            })}
          />
          {errors.password && <span className={styles.errorText}>{errors.password.message}</span>}
        </div>

        <div className={styles.fieldGroup}>
          <label htmlFor="reg-confirmPassword" className={styles.label}>
            Confirm Password
          </label>
          <input
            id="reg-confirmPassword"
            type="password"
            className={`${styles.input} ${errors.confirmPassword ? styles.inputError : ''}`}
            placeholder="••••••••"
            {...register('confirmPassword', {
              required: 'Please confirm your password',
              validate: (val) => val === passwordValue || 'Passwords do not match',
            })}
          />
          {errors.confirmPassword && (
            <span className={styles.errorText}>{errors.confirmPassword.message}</span>
          )}
        </div>
      </div>

      <div className={styles.fieldGroup}>
        <label className={styles.label}>Account Role</label>
        <div className={styles.roleGroup}>
          <label
            className={`${styles.roleCard} ${
              selectedRole === 'ROLE_AUDITOR' ? styles.roleCardActive : ''
            }`}
          >
            <input
              type="radio"
              value="ROLE_AUDITOR"
              className={styles.radioInput}
              {...register('role')}
            />
            <span className={styles.roleTitle}>Auditor</span>
          </label>

          <label
            className={`${styles.roleCard} ${
              selectedRole === 'ROLE_ADMIN' ? styles.roleCardActive : ''
            }`}
          >
            <input
              type="radio"
              value="ROLE_ADMIN"
              className={styles.radioInput}
              {...register('role')}
            />
            <span className={styles.roleTitle}>Administrator</span>
          </label>
        </div>
      </div>

      <button type="submit" className={`btn-primary ${styles.submitBtn}`} disabled={loading}>
        {loading ? <LoadingSpinner message="" /> : 'Create Account'}
      </button>

      <p className={styles.footerText}>
        Already have an account?{' '}
        <Link to="/login" className={styles.footerLink}>
          Sign in here
        </Link>
      </p>
    </form>
  );
}

export default RegisterForm;
