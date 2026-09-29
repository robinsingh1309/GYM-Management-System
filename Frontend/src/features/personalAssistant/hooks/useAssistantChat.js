import { useCallback, useRef, useState } from 'react';

import { sendChatQuestion } from '../api/chatApi';
import { CHAT_ERROR_MESSAGE, MAX_QUESTION_LENGTH } from '../constants';

const createMessage = (role, content, status = 'sent', sources = []) => ({
  id: crypto.randomUUID(),
  role,
  content,
  timestamp: new Date().toISOString(),
  status,
  sources,
});

function useAssistantChat() {
  const [messages, setMessages] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const requestInProgress = useRef(false);

  const requestAnswer = useCallback(async (question, appendUserMessage = true) => {
    if (requestInProgress.current) {
      return false;
    }

    requestInProgress.current = true;
    setIsLoading(true);

    if (appendUserMessage) {
      setMessages((current) => [...current, createMessage('user', question)]);
    }

    try {
      const response = await sendChatQuestion(question);
      const { answer, sources = [] } = response.data;

      setMessages((current) => [
        ...current,
        createMessage('assistant', answer, 'sent', sources),
      ]);
      return true;
    } catch {
      setMessages((current) => [
        ...current,
        createMessage('assistant', CHAT_ERROR_MESSAGE, 'error'),
      ]);
      return false;
    } finally {
      requestInProgress.current = false;
      setIsLoading(false);
    }
  }, []);

  const sendMessage = useCallback((question) => {
    const trimmedQuestion = question.trim();

    if (!trimmedQuestion || trimmedQuestion.length > MAX_QUESTION_LENGTH) {
      return Promise.resolve(false);
    }

    return requestAnswer(trimmedQuestion);
  }, [requestAnswer]);

  const retryMessage = useCallback((messageId) => {
    if (requestInProgress.current) {
      return Promise.resolve(false);
    }

    const errorIndex = messages.findIndex((message) => message.id === messageId);

    if (errorIndex < 0) {
      return Promise.resolve(false);
    }

    const question = messages
      .slice(0, errorIndex)
      .reverse()
      .find((message) => message.role === 'user')?.content;

    if (!question) {
      return Promise.resolve(false);
    }

    setMessages((current) => current.filter((message) => message.id !== messageId));
    return requestAnswer(question, false);
  }, [messages, requestAnswer]);

  return { messages, isLoading, sendMessage, retryMessage };
}

export default useAssistantChat;
