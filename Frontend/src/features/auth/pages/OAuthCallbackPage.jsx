import { useEffect, useRef } from 'react';
import { Spin } from 'antd';
import { useNavigate, useSearchParams } from 'react-router-dom';

import { exchangeOAuth2Code } from '../api/authApi';
import { useAuth } from '../../../context/AuthContext';


function OAuthCallbackPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { login } = useAuth();
  const exchangeStarted = useRef(false);

  useEffect(() => {
    if (exchangeStarted.current) {
      return;
    }

    exchangeStarted.current = true;

    const code = searchParams.get('code');

    if (!code) {
      navigate('/login?error=GOOGLE_AUTH_FAILED', { replace: true });
      return;
    }

    exchangeOAuth2Code(code)
      .then((response) => {
        login(response.data.token);
        navigate('/dashboard', { replace: true });
      })
      .catch(() => {
        navigate('/login?error=GOOGLE_AUTH_FAILED', { replace: true });
      });
  }, [login, navigate, searchParams]);

  return (
    <div style={{ display: 'grid', minHeight: '100vh', placeItems: 'center' }}>
      <Spin size="large" tip="Completing Google sign-in..." />
    </div>
  );
}

export default OAuthCallbackPage;
