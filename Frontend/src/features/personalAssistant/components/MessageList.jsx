import EmptyState from './EmptyState';
import MessageBubble from './MessageBubble';
import TypingIndicator from './TypingIndicator';

function MessageList({ messages, isLoading, onPromptSelect, onRetry, endRef }) {
  return (
    <div className="assistant-messages" role="log" aria-live="polite" aria-relevant="additions">
      {messages.length === 0 && !isLoading ? (
        <EmptyState disabled={isLoading} onPromptSelect={onPromptSelect} />
      ) : (
        messages.map((message) => (
          <MessageBubble key={message.id} message={message} onRetry={onRetry} />
        ))
      )}

      {isLoading && <TypingIndicator />}
      <div ref={endRef} aria-hidden="true" />
    </div>
  );
}

export default MessageList;
