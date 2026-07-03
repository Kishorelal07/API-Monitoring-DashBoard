import { Card, Col, Empty, Row } from 'antd';
import {
  Chart as ChartJS,
  ArcElement,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler,
} from 'chart.js';
import { Pie, Line } from 'react-chartjs-2';
import dayjs from 'dayjs';

ChartJS.register(
  ArcElement,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler
);

const pieOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { position: 'bottom' },
  },
};

const lineOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { display: false },
    tooltip: {
      callbacks: {
        label: (ctx) => `${ctx.parsed.y} ms`,
      },
    },
  },
  scales: {
    y: {
      beginAtZero: true,
      title: { display: true, text: 'Response Time (ms)' },
    },
  },
};

export default function Charts({ stats, logs, loading }) {
  const hasPieData = stats && (stats.successCount > 0 || stats.failureCount > 0);

  const pieData = {
    labels: ['Success', 'Failure'],
    datasets: [
      {
        data: [stats?.successCount ?? 0, stats?.failureCount ?? 0],
        backgroundColor: ['#52c41a', '#ff4d4f'],
        borderWidth: 0,
      },
    ],
  };

  const sortedLogs = [...(logs || [])].sort(
    (a, b) => new Date(a.requestTimestamp) - new Date(b.requestTimestamp)
  );

  const lineData = {
    labels: sortedLogs.map((log) => dayjs(log.requestTimestamp).format('HH:mm:ss')),
    datasets: [
      {
        label: 'Response Time',
        data: sortedLogs.map((log) => log.responseTime),
        borderColor: '#1677ff',
        backgroundColor: 'rgba(22, 119, 255, 0.1)',
        fill: true,
        tension: 0.3,
        pointRadius: 3,
      },
    ],
  };

  return (
    <Row gutter={[16, 16]}>
      <Col xs={24} lg={10}>
        <Card title="Success vs Failure" bordered={false} loading={loading}>
          <div className="chart-container">
            {hasPieData ? (
              <Pie data={pieData} options={pieOptions} />
            ) : (
              <Empty description="No data for chart" />
            )}
          </div>
        </Card>
      </Col>
      <Col xs={24} lg={14}>
        <Card title="Response Time Trend" bordered={false} loading={loading}>
          <div className="chart-container">
            {sortedLogs.length > 0 ? (
              <Line data={lineData} options={lineOptions} />
            ) : (
              <Empty description="No data for chart" />
            )}
          </div>
        </Card>
      </Col>
    </Row>
  );
}
