import { useMemo, useState } from 'react';
import {
  Avatar,
  Button,
  Drawer,
  FloatButton,
  Input,
  Space,
  Typography,
  message,
} from 'antd';
import { RobotOutlined, UserOutlined } from '@ant-design/icons';
import { askCohereAgent } from '../services/cohereAgent';

const { Text, Paragraph } = Typography;
const { TextArea } = Input;

function buildInitialMessage() {
  return {
    id: 'welcome',
    role: 'assistant',
    text:
      'Hello! I can explain API errors in simple language. I will include the status code whenever it is available.',
  };
}

export default function ChatbotAssistant() {
  const [open, setOpen] = useState(false);
  const [input, setInput] = useState('');
  const [sending, setSending] = useState(false);
  const [messages, setMessages] = useState([buildInitialMessage()]);

  const canSend = useMemo(() => input.trim().length > 0 && !sending, [input, sending]);

  const pushMessage = (role, text) => {
    setMessages((prev) => [...prev, { id: `${role}-${Date.now()}`, role, text }]);
  };

  const onSend = async () => {
    const userText = input.trim();
    if (!userText || sending) return;

    pushMessage('user', userText);
    setInput('');
    setSending(true);

    try {
      const reply = await askCohereAgent(userText);
      pushMessage('assistant', reply);
    } catch (err) {
      const errorText = err?.message || 'Failed to get AI response.';
      pushMessage('assistant', `Sorry, I ran into an issue. ${errorText}`);
      message.error(errorText);
    } finally {
      setSending(false);
    }
  };

  return (
    <>
      <FloatButton
        icon={<RobotOutlined />}
        type="primary"
        tooltip={<span>Open AI Assistant</span>}
        onClick={() => setOpen(true)}
      />

      <Drawer
        title="AI Chat Assistant"
        placement="right"
        width={420}
        onClose={() => setOpen(false)}
        open={open}
      >
        <div className="chatbot-wrapper">
          <div className="chatbot-hint">
            <Text type="secondary">
              Ask in normal language. The assistant explains API problems simply and includes status
              code details.
            </Text>
          </div>

          <div className="chatbot-messages">
            {messages.map((item) => (
              <div
                key={item.id}
                className={`chatbot-message ${
                  item.role === 'user' ? 'chatbot-message-user' : 'chatbot-message-assistant'
                }`}
              >
                <Space align="start" size={8}>
                  <Avatar
                    size="small"
                    icon={item.role === 'user' ? <UserOutlined /> : <RobotOutlined />}
                  />
                  <div className="chatbot-bubble">
                    <Text strong>{item.role === 'user' ? 'You' : 'Assistant'}</Text>
                    <Paragraph className="chatbot-text">{item.text}</Paragraph>
                  </div>
                </Space>
              </div>
            ))}
          </div>

          <Space direction="vertical" style={{ width: '100%' }}>
            <TextArea
              value={input}
              onChange={(event) => setInput(event.target.value)}
              rows={4}
              maxLength={700}
              placeholder="Example: Why did my API fail and what does status code 500 mean?"
              onPressEnter={(event) => {
                if (!event.shiftKey) {
                  event.preventDefault();
                  onSend();
                }
              }}
            />
            <Button type="primary" loading={sending} onClick={onSend} disabled={!canSend} block>
              {sending ? 'Thinking...' : 'Send'}
            </Button>
          </Space>
        </div>
      </Drawer>
    </>
  );
}
