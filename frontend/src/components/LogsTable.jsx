import { Card, Table, Tag, Tooltip } from 'antd';
import { WarningOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';

const SLOW_THRESHOLD_MS = 2000;

export default function LogsTable({ logs, loading }) {
  const columns = [
    {
      title: 'API Name',
      dataIndex: 'apiName',
      key: 'apiName',
      sorter: (a, b) => a.apiName.localeCompare(b.apiName),
      render: (text, record) => (
        <span className={record.responseTime > SLOW_THRESHOLD_MS ? 'slow-api' : ''}>
          {record.responseTime > SLOW_THRESHOLD_MS && (
            <Tooltip title={`Slow API (>${SLOW_THRESHOLD_MS} ms)`}>
              <WarningOutlined style={{ color: '#faad14', marginRight: 6 }} />
            </Tooltip>
          )}
          {text}
        </span>
      ),
    },
    {
      title: 'Work Item',
      dataIndex: 'workItemId',
      key: 'workItemId',
      render: (v) => v || '—',
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      filters: [
        { text: 'SUCCESS', value: 'SUCCESS' },
        { text: 'FAILURE', value: 'FAILURE' },
      ],
      onFilter: (value, record) => record.status === value,
      render: (status) => (
        <Tag color={status === 'SUCCESS' ? 'success' : 'error'}>{status}</Tag>
      ),
    },
    {
      title: 'Response Code',
      dataIndex: 'responseCode',
      key: 'responseCode',
      sorter: (a, b) => (a.responseCode ?? 0) - (b.responseCode ?? 0),
      render: (code) => code ?? '—',
    },
    {
      title: 'Response Time',
      dataIndex: 'responseTime',
      key: 'responseTime',
      defaultSortOrder: 'descend',
      sorter: (a, b) => a.responseTime - b.responseTime,
      render: (ms, record) => (
        <span className={record.responseTime > SLOW_THRESHOLD_MS ? 'slow-api-text' : ''}>
          {ms} ms
        </span>
      ),
    },
    {
      title: 'Timestamp',
      dataIndex: 'requestTimestamp',
      key: 'requestTimestamp',
      sorter: (a, b) => new Date(a.requestTimestamp) - new Date(b.requestTimestamp),
      render: (ts) => dayjs(ts).format('YYYY-MM-DD HH:mm:ss'),
    },
    {
      title: 'Error',
      dataIndex: 'errorMessage',
      key: 'errorMessage',
      ellipsis: true,
      render: (msg) => msg || '—',
    },
  ];

  return (
    <Card title="API Call Logs" bordered={false}>
      <Table
        rowKey="id"
        columns={columns}
        dataSource={logs}
        loading={loading}
        pagination={{
          pageSize: 10,
          showSizeChanger: true,
          showTotal: (total) => `Total ${total} logs`,
          pageSizeOptions: ['10', '20', '50'],
        }}
        locale={{ emptyText: 'No API logs found for the selected filters.' }}
        scroll={{ x: 900 }}
        rowClassName={(record) =>
          record.responseTime > SLOW_THRESHOLD_MS ? 'slow-row' : ''
        }
      />
    </Card>
  );
}
