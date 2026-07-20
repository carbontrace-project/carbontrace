import { useState, useEffect, useCallback } from 'react';
import apiClient from '../api/axiosConfig';
import StatCards from '../components/dashboard/StatCards';
import EmissionsTrendChart from '../components/dashboard/EmissionsTrendChart';
import ModeBreakdownChart from '../components/dashboard/ModeBreakdownChart';
import GoalProgressList from '../components/dashboard/GoalProgressList';
import VendorEmissionsTable from '../components/dashboard/VendorEmissionsTable';
import QuickActions from '../components/dashboard/QuickActions';
import LoadingSpinner from '../components/common/LoadingSpinner';
import ErrorMessage from '../components/common/ErrorMessage';
import styles from './DashboardPage.module.css';

function DashboardPage() {
  const [summary, setSummary] = useState(null);
  const [goals, setGoals] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchDashboardData = useCallback(async () => {
    setLoading(true);
    setError(null);

    try {
      let summaryData = null;
      try {
        const res = await apiClient.get('/api/dashboard/summary');
        summaryData = res.data?.data || res.data;
      } catch {
        const res = await apiClient.get('/api/analytics/dashboard');
        summaryData = res.data?.data || res.data;
      }

      // Standardize monthlyEmissions and breakdowns if object map vs array
      if (summaryData) {
        if (summaryData.monthlyEmissions && !Array.isArray(summaryData.monthlyEmissions)) {
          summaryData.monthlyEmissions = Object.entries(summaryData.monthlyEmissions).map(([key, val]) => ({
            month: key,
            emissionsKgco2e: val,
          }));
        }
        if (summaryData.emissionsByMode && !summaryData.transportModeBreakdown) {
          summaryData.transportModeBreakdown = Object.entries(summaryData.emissionsByMode).map(([mode, val]) => ({
            mode,
            emissionsKgco2e: val,
          }));
        }
        if (summaryData.emissionsByVendor && !summaryData.vendorEmissions) {
          summaryData.vendorEmissions = summaryData.emissionsByVendor.map((v) => ({
            vendorId: v.vendorId,
            vendorName: v.vendorName,
            totalEmissionsKgco2e: v.emissionsKgco2e || v.totalEmissionsKgco2e,
            shipmentCount: v.shipmentCount || 0,
          }));
        }
      }

      setSummary(summaryData);

      try {
        const goalsRes = await apiClient.get('/api/goals');
        const goalsData = goalsRes.data?.data?.content || goalsRes.data?.data || goalsRes.data || [];
        setGoals(Array.isArray(goalsData) ? goalsData : []);
      } catch {
        // Fallback if goals empty
      }
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Failed to load dashboard metrics.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchDashboardData();
  }, [fetchDashboardData]);

  if (loading) {
    return <LoadingSpinner message="Loading carbon analytics dashboard..." />;
  }

  if (error) {
    return <ErrorMessage title="Dashboard Load Error" message={error} onRetry={fetchDashboardData} />;
  }

  return (
    <div className={styles.container}>
      <header className={styles.header}>
        <h1 className={styles.title}>Supply Chain Emissions Analytics</h1>
        <p className={styles.subtitle}>
          Overview of cargo logistics carbon footprint, reduction targets, mode breakdown, and vendor performance
        </p>
      </header>

      <StatCards summary={summary || {}} />

      <QuickActions />

      <div className={styles.chartsGrid}>
        <EmissionsTrendChart data={summary?.monthlyEmissions || []} />
        <ModeBreakdownChart data={summary?.transportModeBreakdown || []} />
      </div>

      <div className={styles.twoColumnGrid}>
        <GoalProgressList goals={goals} />
        <VendorEmissionsTable vendorData={summary?.vendorEmissions || []} />
      </div>
    </div>
  );
}

export default DashboardPage;
