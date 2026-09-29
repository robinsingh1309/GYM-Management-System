import { useState } from 'react';
import { Button, Input } from 'antd';

import { CHAT_PLACEHOLDER, MAX_QUESTION_LENGTH } from '../constants';

const { TextArea } = Input;

function ChatInput({ disabled, inputRef, onSend }) {
  const [question, setQuestion] = useState('');
  const canSend = question.trim().length > 0 && !disabled;

  const submitQuestion = () => {
    if (!canSend) {
      return;
    }

    onSend(question);
    setQuestion('');
  };

  const handleKeyDown = (event) => {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      submitQuestion();
    }
  };

  return (
    <div className="assistant-composer">
      <label htmlFor="assistant-question" className="assistant-visually-hidden">
        Message Personal Assistant
      </label>
      <TextArea
        id="assistant-question"
        ref={inputRef}
        value={question}
        onChange={(event) => setQuestion(event.target.value)}
        onKeyDown={handleKeyDown}
        placeholder={CHAT_PLACEHOLDER}
        maxLength={MAX_QUESTION_LENGTH}
        autoSize={{ minRows: 1, maxRows: 5 }}
        disabled={disabled}
        aria-describedby="assistant-input-help"
      />
      <Button
        className="assistant-send-button"
        type="primary"
        onClick={submitQuestion}
        disabled={!canSend}
        loading={disabled}
        aria-label="Send message"
      >
        Send
      </Button>
      <span id="assistant-input-help" className="assistant-visually-hidden">
        Press Enter to send. Press Shift and Enter for a new line.
      </span>
    </div>
  );
}

export default ChatInput;
