import { useState, useEffect, useCallback } from 'react';
import apiClient from '../api/axiosConfig';
import GoalList from '../components/goals/GoalList';
import GoalForm from '../components/goals/GoalForm';
import ErrorMessage from '../components/common/ErrorMessage';
import styles from './GoalsPage.module.css';

function GoalsPage() {
  const [goals, setGoals] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingGoal, setEditingGoal] = useState(null);
  const [formLoading, setFormLoading] = useState(false);

  const fetchGoals = useCallback(async () => {
    setLoading(true);
    setError(null);

    try {
      const response = await apiClient.get('/api/goals');
      const data = response.data?.data?.content || response.data?.data || response.data || [];
      setGoals(Array.isArray(data) ? data : []);
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Failed to load reduction goals.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchGoals();
  }, [fetchGoals]);

  const handleOpenAddModal = () => {
    setEditingGoal(null);
    setIsModalOpen(true);
  };

  const handleOpenEditModal = (goal) => {
    setEditingGoal(goal);
    setIsModalOpen(true);
  };

  const handleFormSubmit = async (formData) => {
    setFormLoading(true);

    const payload = {
      title: formData.title,
      targetReductionPercentage: Number(formData.targetReductionPercentage),
      baselineEmissionsKgco2e: Number(formData.baselineEmissionsKgco2e),
      startDate: formData.startDate,
      endDate: formData.endDate,
    };

    try {
      if (editingGoal?.id) {
        await apiClient.put(`/api/goals/${editingGoal.id}`, payload);
      } else {
        await apiClient.post('/api/goals', payload);
      }

      setIsModalOpen(false);
      setEditingGoal(null);
      fetchGoals();
    } catch (err) {
      alert(err.response?.data?.message || err.message || 'Failed to save goal.');
    } finally {
      setFormLoading(false);
    }
  };

  return (
    <div className={styles.container}>
      <header className={styles.header}>
        <div className={styles.titleGroup}>
          <h1 className={styles.title}>Emissions Reduction Goals</h1>
          <p className={styles.subtitle}>
            Establish Net-Zero corporate decarbonization targets and monitor supply chain reduction progress
          </p>
        </div>
        <button type="button" className="btn-primary" onClick={handleOpenAddModal}>
          + Create Goal
        </button>
      </header>

      {error && <ErrorMessage title="Goals Load Error" message={error} onRetry={fetchGoals} />}

      <GoalList goals={goals} onEditGoal={handleOpenEditModal} loading={loading} />

      <GoalForm
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSubmit={handleFormSubmit}
        goal={editingGoal}
        loading={formLoading}
      />
    </div>
  );
}

export default GoalsPage;
