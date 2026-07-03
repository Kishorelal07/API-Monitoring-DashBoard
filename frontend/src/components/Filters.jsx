import { Button, Card, Col, DatePicker, Input, Row, Select, Space } from 'antd';
import { FilterOutlined, ReloadOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';

const { Search } = Input;

export default function Filters({ filters, onChange, onApply, onReset, loading }) {
  return (
    <Card title="Filters" bordered={false} size="small">
      <Row gutter={[16, 16]} align="bottom">
        <Col xs={24} sm={12} md={6}>
          <label className="filter-label">Date</label>
          <DatePicker
            style={{ width: '100%' }}
            value={dayjs(filters.date)}
            onChange={(d) => onChange({ ...filters, date: d ? d.format('YYYY-MM-DD') : dayjs().format('YYYY-MM-DD') })}
            allowClear={false}
          />
        </Col>
        <Col xs={24} sm={12} md={8}>
          <label className="filter-label">API Name</label>
          <Search
            placeholder="Search by API name"
            allowClear
            value={filters.apiName}
            onChange={(e) => onChange({ ...filters, apiName: e.target.value })}
            onSearch={onApply}
          />
        </Col>
        <Col xs={24} sm={12} md={6}>
          <label className="filter-label">Status</label>
          <Select
            style={{ width: '100%' }}
            placeholder="All statuses"
            allowClear
            value={filters.status || undefined}
            onChange={(value) => onChange({ ...filters, status: value || '' })}
            options={[
              { value: 'SUCCESS', label: 'SUCCESS' },
              { value: 'FAILURE', label: 'FAILURE' },
            ]}
          />
        </Col>
        <Col xs={24} sm={12} md={4}>
          <Space>
            <Button type="primary" icon={<FilterOutlined />} onClick={onApply} loading={loading}>
              Apply
            </Button>
            <Button icon={<ReloadOutlined />} onClick={onReset}>
              Today
            </Button>
          </Space>
        </Col>
      </Row>
    </Card>
  );
}
