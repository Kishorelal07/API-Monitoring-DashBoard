import axios from 'axios';

const COHERE_API_URL = 'https://api.cohere.com/v2/chat';
const COHERE_MODEL = import.meta.env.VITE_COHERE_MODEL || 'command-a-03-2025';
const COHERE_API_KEY = import.meta.env.VITE_COHERE_API_KEY;

const NON_TECHNICAL_BOUNDARY_PROMPT = `
You are an API monitoring support assistant for non-technical users.

Boundaries of AI agent (must follow):
1) If API has any error, explain it in simple language.
2) Always include the HTTP status code when discussing API errors.
3) Avoid technical jargon unless the user asks for technical details.
4) Keep responses short, clear, and action-oriented.
5) If status code is missing, clearly say it is not available.
`;

function extractAssistantText(responseData) {
  const content = responseData?.message?.content;
  if (!Array.isArray(content)) return '';
  const textParts = content
    .filter((item) => item?.type === 'text' && typeof item?.text === 'string')
    .map((item) => item.text.trim())
    .filter(Boolean);
  return textParts.join('\n\n');
}

export function buildUserPrompt(userInput) {
  return `User question: ${userInput}

Please respond as if speaking to a non-technical person.`;
}

export async function askCohereAgent(userInput) {
  if (!COHERE_API_KEY) {
    throw new Error('Cohere API key missing. Set VITE_COHERE_API_KEY in frontend .env.');
  }

  try {
    const response = await axios.post(
      COHERE_API_URL,
      {
        model: COHERE_MODEL,
        messages: [
          { role: 'system', content: NON_TECHNICAL_BOUNDARY_PROMPT.trim() },
          { role: 'user', content: buildUserPrompt(userInput) },
        ],
        temperature: 0.2,
      },
      {
        headers: {
          Authorization: `Bearer ${COHERE_API_KEY}`,
          'Content-Type': 'application/json',
        },
        timeout: 20000,
      }
    );

    const assistantText = extractAssistantText(response.data);
    if (!assistantText) {
      return 'I could not generate a response right now. Please try again.';
    }
    return assistantText;
  } catch (error) {
    const statusCode = error?.response?.status;
    const rawMessage =
      error?.response?.data?.message ||
      error?.response?.data?.error ||
      error?.message ||
      'Unexpected error while contacting AI assistant.';
    const modelRemoved =
      statusCode === 404 &&
      typeof rawMessage === 'string' &&
      rawMessage.toLowerCase().includes('was removed');
    const message = modelRemoved
      ? `${rawMessage} Please update VITE_COHERE_MODEL to a live model such as command-a-03-2025.`
      : rawMessage;

    throw new Error(
      statusCode
        ? `AI service error (${statusCode}): ${message}`
        : `AI service error: ${message}`
    );
  }
}
