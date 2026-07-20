import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
} from 'recharts';
import { formatNumber } from '../../utils/formatters';
import styles from './EmissionsTrendChart.module.css';

function EmissionsTrendChart({ data = [] }) {
  const chartData = (data || []).map((item) => ({
    name: item.month ? `${item.month} ${item.year || ''}` : 'Month',
    emissions: item.emissionsKgco2e || 0,
    shipments: item.shipmentCount || 0,
  }));

  return (
    <div className={styles.card}>
      <h3 className={styles.title}>Monthly Carbon Emissions Trend</h3>
      <div className={styles.chartWrapper}>
        <ResponsiveContainer width="100%" height="100%">
          <BarChart data={chartData} margin={{ top: 10, right: 20, left: 0, bottom: 0 }}>
            <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" />
            <XAxis dataKey="name" stroke="var(--text-muted)" fontSize={12} />
            <YAxis
              stroke="var(--text-muted)"
              fontSize={12}
              tickFormatter={(val) => `${formatNumber(val / 1000, 1)}k`}
            />
            <Tooltip
              formatter={(value) => [`${formatNumber(value, 2)} kgCO₂e`, 'Emissions']}
              contentStyle={{
                backgroundColor: 'var(--bg-surface)',
                border: '1px solid var(--border)',
                borderRadius: 'var(--radius-md)',
              }}
            />
            <Bar dataKey="emissions" fill="var(--primary)" radius={[4, 4, 0, 0]} />
          </BarChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}

export default EmissionsTrendChart;
