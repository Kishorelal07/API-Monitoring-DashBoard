import { useCallback, useEffect, useState } from 'react';
import { Typography, Switch, Space, message } from 'antd';
import { SyncOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import { fetchLogs, fetchStats } from '../services/api';
import SummaryCards from './SummaryCards';
import Filters from './Filters';
import Charts from './Charts';
import LogsTable from './LogsTable';

const { Text } = Typography;

const AUTO_REFRESH_MS = 30000;

const defaultFilters = () => ({
  date: dayjs().format('YYYY-MM-DD'),
  apiName: '',
  status: '',
});

export default function Dashboard({ refreshKey = 0 }) {
  const [filters, setFilters] = useState(defaultFilters);
  const [appliedFilters, setAppliedFilters] = useState(defaultFilters);
  const [logs, setLogs] = useState([]);
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(false);
  const [autoRefresh, setAutoRefresh] = useState(true);
  const [lastUpdated, setLastUpdated] = useState(null);

  const loadData = useCallback(async () => {
    setLoading(true);
    try {
      const [logsData, statsData] = await Promise.all([
        fetchLogs(appliedFilters),
        fetchStats(appliedFilters),
      ]);
      setLogs(logsData);
      setStats(statsData);
      setLastUpdated(new Date());
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Failed to load dashboard data';
      message.error(msg);
    } finally {
      setLoading(false);
    }
  }, [appliedFilters]);

  useEffect(() => {
    loadData();
  }, [loadData, refreshKey]);

  useEffect(() => {
    if (!autoRefresh) return undefined;
    const timer = setInterval(loadData, AUTO_REFRESH_MS);
    return () => clearInterval(timer);
  }, [autoRefresh, loadData]);

  const handleApply = () => setAppliedFilters({ ...filters });

  const handleReset = () => {
    const today = defaultFilters();
    setFilters(today);
    setAppliedFilters(today);
  };

  const isToday = appliedFilters.date === dayjs().format('YYYY-MM-DD');

  return (
    <div className="content-inner">
      <div className="dashboard-toolbar">
        <Text type="secondary" className="view-label">
          {isToday ? "Showing today's logs" : `Showing logs for ${appliedFilters.date}`}
        </Text>
        <Space size="large">
          <Space>
            <SyncOutlined spin={loading} />
            <Text type="secondary">
              {lastUpdated ? `Updated ${dayjs(lastUpdated).format('HH:mm:ss')}` : 'Loading…'}
            </Text>
          </Space>
          <Space>
            <Text>Auto-refresh (30s)</Text>
            <Switch checked={autoRefresh} onChange={setAutoRefresh} />
          </Space>
        </Space>
      </div>

      <div className="section">
        <SummaryCards stats={stats} loading={loading && !stats} />
      </div>

      <div className="section">
        <Filters
          filters={filters}
          onChange={setFilters}
          onApply={handleApply}
          onReset={handleReset}
          loading={loading}
        />
      </div>

      <div className="section">
        <Charts stats={stats} logs={logs} loading={loading && logs.length === 0} />
      </div>

      <div className="section">
        <LogsTable logs={logs} loading={loading} />
      </div>
    </div>
  );
}
