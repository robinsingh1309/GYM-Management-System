import { Button } from 'antd';

function MessageBubble({ message, onRetry }) {

  return (
    <article className={`assistant-message assistant-message-${message.role}${message.status === 'error' ? ' assistant-message-error' : ''}`}>
      <div className="assistant-message-label">
        {message.role === 'user' ? 'You' : 'Personal Assistant'}
      </div>
      <div className="assistant-message-bubble">
        <p>{message.content}</p>

        {message.status === 'error' && (
          <Button size="small" onClick={() => onRetry(message.id)}>
            Retry
          </Button>
        )}
      </div>
      <time dateTime={message.timestamp}>
        {new Date(message.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
      </time>
    </article>
  );
}

export default MessageBubble;
