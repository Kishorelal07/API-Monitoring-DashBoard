import { useState } from 'react';
import {
  Button, Card, Col, Form, Input, InputNumber, Row, Select, Space,
  Typography, message, Divider, Tag,
} from 'antd';
import { SaveOutlined, ReloadOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import { submitLog } from '../services/api';

const { Title, Text, Paragraph } = Typography;

const MOCK_PRESETS = [
  { label: 'CBS Balance — Success', apiName: 'CBS_ACCOUNT_BALANCE', workItemId: 'WI-MOCK-001', responseCode: 200, status: 'SUCCESS', responseTime: 145, errorMessage: '' },
  { label: 'Fintech Payment — Failure', apiName: 'FINTECH_PAYMENT', workItemId: 'WI-MOCK-002', responseCode: 504, status: 'FAILURE', responseTime: 30012, errorMessage: 'Gateway timeout' },
  { label: 'CBS Fund Transfer — Success', apiName: 'CBS_FUND_TRANSFER', workItemId: 'WI-MOCK-003', responseCode: 200, status: 'SUCCESS', responseTime: 320, errorMessage: '' },
  { label: 'Fintech KYC — Success', apiName: 'FINTECH_KYC_VERIFY', workItemId: 'WI-MOCK-004', responseCode: 200, status: 'SUCCESS', responseTime: 890, errorMessage: '' },
  { label: 'CBS Loan — Failure', apiName: 'CBS_LOAN_ENQUIRY', workItemId: 'WI-MOCK-005', responseCode: 500, status: 'FAILURE', responseTime: 2100, errorMessage: 'Internal server error from CBS' },
];

function buildPayload(values) {
  const responseTimestamp = dayjs();
  const requestTimestamp = responseTimestamp.subtract(values.responseTime, 'millisecond');
  return {
    apiName: values.apiName.trim(),
    workItemId: values.workItemId?.trim() || null,
    requestTimestamp: requestTimestamp.format('YYYY-MM-DDTHH:mm:ss'),
    responseTimestamp: responseTimestamp.format('YYYY-MM-DDTHH:mm:ss'),
    responseCode: values.responseCode,
    status: values.status,
    errorMessage: values.status === 'FAILURE' ? (values.errorMessage?.trim() || 'Mock failure') : null,
  };
}

export default function AddMockLog({ onLogSaved }) {
  const [form] = Form.useForm();
  const [submitting, setSubmitting] = useState(false);
  const [lastSaved, setLastSaved] = useState(null);
  const status = Form.useWatch('status', form);

  const applyPreset = (preset) => {
    form.setFieldsValue(preset);
    setLastSaved(null);
  };

  const handleSubmit = async (values) => {
    setSubmitting(true);
    try {
      const saved = await submitLog(buildPayload(values));
      setLastSaved(saved);
      message.success(`Log saved — ID ${saved.id}`);
      onLogSaved?.();
    } catch (err) {
      message.error(err.response?.data?.message || err.message || 'Failed to save log');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="content-inner">
      <Title level={4}>Add Mock API Log</Title>
      <Paragraph type="secondary">
        Submit test data to <Text code>POST /api/log</Text> without integrating your Java app.
        Saved logs appear on the Dashboard (today&apos;s view).
      </Paragraph>

      <Row gutter={[20, 20]}>
        <Col xs={24} lg={14}>
          <Card title="Log Details" bordered={false}>
            <Form
              form={form}
              layout="vertical"
              onFinish={handleSubmit}
              initialValues={{
                apiName: '',
                workItemId: '',
                status: 'SUCCESS',
                responseCode: 200,
                responseTime: 250,
                errorMessage: '',
              }}
            >
              <Row gutter={16}>
                <Col span={12}>
                  <Form.Item name="apiName" label="API Name" rules={[{ required: true, message: 'Required' }]}>
                    <Input placeholder="e.g. CBS_ACCOUNT_BALANCE" />
                  </Form.Item>
                </Col>
                <Col span={12}>
                  <Form.Item name="workItemId" label="Work Item ID">
                    <Input placeholder="e.g. WI-2026-001234" />
                  </Form.Item>
                </Col>
                <Col span={8}>
                  <Form.Item name="status" label="Status" rules={[{ required: true }]}>
                    <Select options={[
                      { value: 'SUCCESS', label: 'SUCCESS' },
                      { value: 'FAILURE', label: 'FAILURE' },
                    ]} />
                  </Form.Item>
                </Col>
                <Col span={8}>
                  <Form.Item name="responseCode" label="Response Code">
                    <InputNumber min={100} max={599} style={{ width: '100%' }} />
                  </Form.Item>
                </Col>
                <Col span={8}>
                  <Form.Item name="responseTime" label="Response Time (ms)">
                    <InputNumber min={0} style={{ width: '100%' }} />
                  </Form.Item>
                </Col>
                {status === 'FAILURE' && (
                  <Col span={24}>
                    <Form.Item name="errorMessage" label="Error Message">
                      <Input placeholder="e.g. Gateway timeout" />
                    </Form.Item>
                  </Col>
                )}
              </Row>
              <Space>
                <Button type="primary" htmlType="submit" icon={<SaveOutlined />} loading={submitting}>
                  Save Log
                </Button>
                <Button icon={<ReloadOutlined />} onClick={() => { form.resetFields(); setLastSaved(null); }}>
                  Reset
                </Button>
              </Space>
            </Form>
          </Card>

          {lastSaved && (
            <Card title="Last Saved Response" bordered={false} style={{ marginTop: 20 }}>
              <pre className="json-block">{JSON.stringify(lastSaved, null, 2)}</pre>
            </Card>
          )}
        </Col>

        <Col xs={24} lg={10}>
          <Card title="Quick Mock Presets" bordered={false}>
            <Text type="secondary">Click a preset to fill the form, then Save Log.</Text>
            <Divider style={{ margin: '12px 0' }} />
            <Space direction="vertical" style={{ width: '100%' }} size="small">
              {MOCK_PRESETS.map((preset) => (
                <Button
                  key={preset.label}
                  block
                  onClick={() => applyPreset(preset)}
                  style={{ textAlign: 'left', height: 'auto', padding: '10px 12px' }}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between', width: '100%' }}>
                    <span>{preset.label}</span>
                    <Tag color={preset.status === 'SUCCESS' ? 'success' : 'error'}>{preset.responseCode}</Tag>
                  </div>
                </Button>
              ))}
            </Space>
          </Card>
        </Col>
      </Row>
    </div>
  );
}
