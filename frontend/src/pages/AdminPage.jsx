import { useState } from 'react';
import UserManagementTab from '../components/admin/UserManagementTab';
import EmissionFactorTab from '../components/admin/EmissionFactorTab';
import MarketplaceManagementTab from '../components/admin/MarketplaceManagementTab';
import styles from './AdminPage.module.css';

function AdminPage() {
  const [activeTab, setActiveTab] = useState('users'); // users | factors | marketplace

  return (
    <div className={styles.container}>
      <header className={styles.header}>
        <h1 className={styles.title}>Admin Management Console</h1>
        <p className={styles.subtitle}>
          System configuration, user role permissions, GLEC emission factor management, and carbon marketplace inventory control
        </p>
      </header>

      <div className={styles.tabs}>
        <button
          type="button"
          className={`${styles.tabBtn} ${activeTab === 'users' ? styles.tabBtnActive : ''}`}
          onClick={() => setActiveTab('users')}
        >
          User Permissions
        </button>
        <button
          type="button"
          className={`${styles.tabBtn} ${activeTab === 'factors' ? styles.tabBtnActive : ''}`}
          onClick={() => setActiveTab('factors')}
        >
          Emission Factors
        </button>
        <button
          type="button"
          className={`${styles.tabBtn} ${activeTab === 'marketplace' ? styles.tabBtnActive : ''}`}
          onClick={() => setActiveTab('marketplace')}
        >
          Marketplace Inventory
        </button>
      </div>

      {activeTab === 'users' && <UserManagementTab />}
      {activeTab === 'factors' && <EmissionFactorTab />}
      {activeTab === 'marketplace' && <MarketplaceManagementTab />}
    </div>
  );
}

export default AdminPage;
