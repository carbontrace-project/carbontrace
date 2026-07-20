import { useState, useEffect, useCallback } from 'react';
import apiClient from '../../api/axiosConfig';
import LoadingSpinner from '../common/LoadingSpinner';
import ErrorMessage from '../common/ErrorMessage';
import Pagination from '../common/Pagination';
import styles from './UserManagementTab.module.css';

function UserManagementTab() {
  const [users, setUsers] = useState([]);
  const [roleFilter, setRoleFilter] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchUsers = useCallback(async (page = 0, role = '') => {
    setLoading(true);
    setError(null);

    try {
      let response;
      try {
        response = await apiClient.get('/api/users', {
          params: { page, size: 10, role: role || undefined },
        });
      } catch {
        response = await apiClient.get('/api/admin/users', {
          params: { page, size: 10, role: role || undefined },
        });
      }

      const resData = response.data?.data || response.data;
      const content = resData?.content || resData || [];
      const pageInfo = resData?.page !== undefined ? resData : { page: 0, totalPages: 1, totalElements: content.length };

      setUsers(content);
      setCurrentPage(pageInfo.page || 0);
      setTotalPages(pageInfo.totalPages || 1);
      setTotalElements(pageInfo.totalElements || content.length);
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Failed to load user accounts.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchUsers(currentPage, roleFilter);
  }, [fetchUsers, currentPage, roleFilter]);

  const handleToggleRole = async (user) => {
    const newRole = user.role === 'ROLE_ADMIN' ? 'ROLE_AUDITOR' : 'ROLE_ADMIN';
    try {
      await apiClient.put(`/api/users/${user.id}/role`, { role: newRole });
      fetchUsers(currentPage, roleFilter);
    } catch (err) {
      alert(err.response?.data?.message || err.message || 'Failed to update user role.');
    }
  };

  return (
    <div className={styles.container}>
      <div className={styles.toolbar}>
        <label htmlFor="user-role-filter" className={styles.filterLabel}>
          Filter by Role:
        </label>
        <select
          id="user-role-filter"
          className={styles.select}
          value={roleFilter}
          onChange={(e) => {
            setRoleFilter(e.target.value);
            setCurrentPage(0);
          }}
        >
          <option value="">All Roles</option>
          <option value="ROLE_AUDITOR">Auditor (ROLE_AUDITOR)</option>
          <option value="ROLE_ADMIN">Admin (ROLE_ADMIN)</option>
        </select>
      </div>

      {error && <ErrorMessage title="User Load Error" message={error} onRetry={() => fetchUsers(currentPage, roleFilter)} />}

      {loading ? (
        <LoadingSpinner message="Loading user accounts..." />
      ) : (
        <div className={styles.tableWrapper}>
          <table className={styles.table}>
            <thead>
              <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Email</th>
                <th>Company</th>
                <th>Current Role</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {users.map((u) => (
                <tr key={u.id}>
                  <td>#{u.id}</td>
                  <td>
                    <strong>
                      {u.firstName} {u.lastName}
                    </strong>
                  </td>
                  <td>{u.email}</td>
                  <td>{u.companyName || 'N/A'}</td>
                  <td>
                    <span
                      className={`${styles.badge} ${
                        u.role === 'ROLE_ADMIN' ? styles.badgeAdmin : styles.badgeAuditor
                      }`}
                    >
                      {u.role === 'ROLE_ADMIN' ? 'Admin' : 'Auditor'}
                    </span>
                  </td>
                  <td>
                    <button
                      type="button"
                      className={`btn-secondary ${styles.actionBtn}`}
                      onClick={() => handleToggleRole(u)}
                    >
                      Set to {u.role === 'ROLE_ADMIN' ? 'Auditor' : 'Admin'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <Pagination
        currentPage={currentPage}
        totalPages={totalPages}
        totalElements={totalElements}
        pageSize={10}
        onPageChange={(page) => setCurrentPage(page)}
      />
    </div>
  );
}

export default UserManagementTab;
