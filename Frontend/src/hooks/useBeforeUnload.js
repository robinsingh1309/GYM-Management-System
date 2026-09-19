import { useEffect } from 'react';

export function useBeforeUnload(enabled) {
  useEffect(() => {
    const handleBeforeUnload = (event) => {
      if (enabled) {
        event.preventDefault();
        event.returnValue = '';
      }
    };

    window.addEventListener('beforeunload', handleBeforeUnload);
    return () => window.removeEventListener('beforeunload', handleBeforeUnload);
  }, [enabled]);
}
