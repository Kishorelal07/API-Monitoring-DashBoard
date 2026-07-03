import { useState } from 'react';
import { Layout, Menu, Typography } from 'antd';
import { DashboardOutlined, PlusCircleOutlined } from '@ant-design/icons';
import Dashboard from './components/Dashboard';
import AddMockLog from './components/AddMockLog';
import ChatbotAssistant from './components/ChatbotAssistant';

const { Header, Content } = Layout;
const { Title } = Typography;

const MENU_ITEMS = [
  { key: 'dashboard', icon: <DashboardOutlined />, label: 'Dashboard' },
  { key: 'add-mock', icon: <PlusCircleOutlined />, label: 'Add Mock Log' },
];

export default function App() {
  const [page, setPage] = useState('dashboard');
  const [refreshKey, setRefreshKey] = useState(0);

  return (
    <Layout className="dashboard-layout">
      <Header className="dashboard-header">
        <div className="header-inner">
          <Title level={3} className="header-title">
            API Monitoring Dashboard
          </Title>
          <Menu
            theme="dark"
            mode="horizontal"
            selectedKeys={[page]}
            items={MENU_ITEMS}
            onClick={({ key }) => setPage(key)}
            className="header-menu"
          />
        </div>
      </Header>

      <Content className="dashboard-content">
        {page === 'dashboard' ? (
          <Dashboard refreshKey={refreshKey} />
        ) : (
          <AddMockLog onLogSaved={() => setRefreshKey((k) => k + 1)} />
        )}
      </Content>
      <ChatbotAssistant />
    </Layout>
  );
}
