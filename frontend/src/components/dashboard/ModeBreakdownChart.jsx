import {
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  Tooltip,
  Legend,
} from 'recharts';
import { formatNumber } from '../../utils/formatters';
import styles from './ModeBreakdownChart.module.css';

const COLORS = ['#1b4332', '#2d6a4f', '#52b788', '#d84315'];

function ModeBreakdownChart({ data = [] }) {
  const chartData = (data || []).map((item) => ({
    name: item.mode || 'Unknown',
    value: item.emissionsKgco2e || 0,
    percentage: item.percentage || 0,
  }));

  return (
    <div className={styles.card}>
      <h3 className={styles.title}>Emissions by Transport Mode</h3>
      <div className={styles.chartWrapper}>
        <ResponsiveContainer width="100%" height="100%">
          <PieChart>
            <Pie
              data={chartData}
              cx="50%"
              cy="50%"
              innerRadius={60}
              outerRadius={90}
              paddingAngle={4}
              dataKey="value"
            >
              {chartData.map((entry, index) => (
                <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
              ))}
            </Pie>
            <Tooltip
              formatter={(value) => [`${formatNumber(value, 2)} kgCO₂e`, 'Emissions']}
              contentStyle={{
                backgroundColor: 'var(--bg-surface)',
                border: '1px solid var(--border)',
                borderRadius: 'var(--radius-md)',
              }}
            />
            <Legend verticalAlign="bottom" height={36} />
          </PieChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}

export default ModeBreakdownChart;
