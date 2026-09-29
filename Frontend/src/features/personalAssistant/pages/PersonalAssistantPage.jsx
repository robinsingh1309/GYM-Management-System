import { useEffect, useRef } from 'react';

import ChatInput from '../components/ChatInput';
import MessageList from '../components/MessageList';
import useAssistantChat from '../hooks/useAssistantChat';
import '../components/PersonalAssistant.css';

function PersonalAssistantPage() {
  const { messages, isLoading, sendMessage, retryMessage } = useAssistantChat();
  const inputRef = useRef(null);
  const endRef = useRef(null);

  useEffect(() => {
    endRef.current?.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
  }, [messages.length, isLoading]);

  useEffect(() => {
    if (!isLoading) {
      inputRef.current?.focus();
    }
  }, [isLoading]);

  return (
    <section className="assistant-page" aria-labelledby="assistant-page-title">
      <header className="assistant-header">
        <div>
          <p className="assistant-eyebrow">FitManager</p>
          <h1 id="assistant-page-title">Personal Assistant</h1>
          <p>Ask questions and get answers</p>
        </div>
      </header>

      <div className="assistant-chat-panel">
        <MessageList
          messages={messages}
          isLoading={isLoading}
          onPromptSelect={sendMessage}
          onRetry={retryMessage}
          endRef={endRef}
        />
        <ChatInput disabled={isLoading} inputRef={inputRef} onSend={sendMessage} />
      </div>
    </section>
  );
}

export default PersonalAssistantPage;
