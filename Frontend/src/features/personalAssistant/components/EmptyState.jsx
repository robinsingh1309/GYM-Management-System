import { Button } from 'antd';

import { CHAT_GREETING, SUGGESTED_PROMPTS } from '../constants';

function EmptyState({ disabled, onPromptSelect }) {
  return (
    <section className="assistant-empty-state" aria-labelledby="assistant-greeting">
      <div className="assistant-empty-icon" aria-hidden="true">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
          <path d="M21 15a4 4 0 0 1-4 4H8l-5 3V7a4 4 0 0 1 4-4h10a4 4 0 0 1 4 4Z" />
          <path d="M8 9h8M8 13h5" />
        </svg>
      </div>
      <h2 id="assistant-greeting">{CHAT_GREETING}</h2>
      <p></p>
      <div className="assistant-suggestions" aria-label="Suggested questions">
        {SUGGESTED_PROMPTS.map((prompt) => (
          <Button key={prompt} disabled={disabled} onClick={() => onPromptSelect(prompt)}>
            {prompt}
          </Button>
        ))}
      </div>
    </section>
  );
}

export default EmptyState;
