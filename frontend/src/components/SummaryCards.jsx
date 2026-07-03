import { Card, Col, Row, Statistic } from 'antd';
import {
  ApiOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  FieldTimeOutlined,
} from '@ant-design/icons';

export default function SummaryCards({ stats, loading }) {
  return (
    <Row gutter={[16, 16]}>
      <Col xs={24} sm={12} lg={6}>
        <Card bordered={false} loading={loading}>
          <Statistic
            title="Total API Calls"
            value={stats?.totalCalls ?? 0}
            prefix={<ApiOutlined style={{ color: '#1677ff' }} />}
          />
        </Card>
      </Col>
      <Col xs={24} sm={12} lg={6}>
        <Card bordered={false} loading={loading}>
          <Statistic
            title="Success Count"
            value={stats?.successCount ?? 0}
            valueStyle={{ color: '#52c41a' }}
            prefix={<CheckCircleOutlined />}
          />
        </Card>
      </Col>
      <Col xs={24} sm={12} lg={6}>
        <Card bordered={false} loading={loading}>
          <Statistic
            title="Failure Count"
            value={stats?.failureCount ?? 0}
            valueStyle={{ color: '#ff4d4f' }}
            prefix={<CloseCircleOutlined />}
          />
        </Card>
      </Col>
      <Col xs={24} sm={12} lg={6}>
        <Card bordered={false} loading={loading}>
          <Statistic
            title="Avg Response Time"
            value={stats?.avgResponseTime ?? 0}
            suffix="ms"
            precision={0}
            prefix={<FieldTimeOutlined style={{ color: '#722ed1' }} />}
          />
        </Card>
      </Col>
    </Row>
  );
}
