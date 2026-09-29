function TypingIndicator() {
  return (
    <div className="assistant-typing" role="status" aria-label="Personal Assistant is typing">
      <span />
      <span />
      <span />
      <span className="assistant-visually-hidden">Personal Assistant is typing</span>
    </div>
  );
}

export default TypingIndicator;
