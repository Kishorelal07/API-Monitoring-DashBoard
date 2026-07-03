import axios from 'axios';
import dayjs from 'dayjs';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '';

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
});

export function buildLogParams({ date, apiName, status }) {
  const params = {};
  const today = dayjs().format('YYYY-MM-DD');

  if (date && date !== today) {
    params.date = date;
  }
  if (apiName?.trim()) {
    params.apiName = apiName.trim();
  }
  if (status) {
    params.status = status;
  }
  return params;
}

export async function fetchLogs(filters = {}) {
  const params = buildLogParams(filters);
  const { data } = await api.get('/api/logs', { params });
  return data;
}

export async function fetchStats(filters = {}) {
  const params = buildLogParams(filters);
  const { data } = await api.get('/api/stats', { params });
  return data;
}

export async function submitLog(logData) {
  const { data } = await api.post('/api/log', logData);
  return data;
}

export default api;
