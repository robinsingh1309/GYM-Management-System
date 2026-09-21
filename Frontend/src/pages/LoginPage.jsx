import { useState } from 'react';
import { Input, Button, ConfigProvider } from 'antd';
import { login as loginApi } from '../api/authApi';
import { API_BASE_URL } from '../api/axiosClient';
import { useAuth } from '../context/AuthContext.jsx';
import { useNavigate } from 'react-router-dom';
import heroAthlete from '../assets/hero-athlete.jpg';
import '../components/auth/LoginPage.css';

const OAUTH_ERROR_MESSAGES = {
  EMAIL_ALREADY_REGISTERED:
    'This email is already registered. Please sign in using your email and password.',
  ACCOUNT_INACTIVE: 'This account is inactive. Please contact an administrator.',
  INVALID_GOOGLE_IDENTITY: 'Google did not provide the required account information.',
  GOOGLE_AUTH_FAILED: 'Google sign-in failed. Please try again.',
};

function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(() => {
    const errorCode = new URLSearchParams(window.location.search).get('error');
    return OAUTH_ERROR_MESSAGES[errorCode] || '';
  });
  const [loading, setLoading] = useState(false);

  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = () => {
    if (!email || !password) {
      setError('Enter your email and password to continue.');
      return;
    }
    setError('');
    setLoading(true);

    loginApi({ email, password })
      .then((response) => {
        login(response.data.token);
        navigate('/dashboard');
      })
      .catch((err) => {
        console.error('Login failed:', err);
        setError("That email and password combination didn't work.");
      })
      .finally(() => setLoading(false));
  };

  const handleGoogleLogin = () => {
    window.location.href = `${API_BASE_URL}/oauth2/authorization/google`;
  };

  const handleKeyDown = (event) => {
    if (event.key === 'Enter') handleSubmit();
  };

  return (
    <ConfigProvider
      theme={{
        token: {
          colorPrimary: '#FF6A3D',
          borderRadius: 10,
          fontFamily: "'Inter', -apple-system, BlinkMacSystemFont, sans-serif",
        },
        components: {
          Input: {
            controlHeight: 48,
            colorBorder: '#2A2D38',
            colorBgContainer: '#1B1E27',
            colorText: '#F5F5F7',
            colorTextPlaceholder: '#6B6E79',
            activeBorderColor: '#FF6A3D',
            hoverBorderColor: '#FF6A3D',
          },
          Button: { controlHeight: 48, fontWeight: 600 },
        },
      }}
    >
      <div className="fm-shell">
        <div className="fm-form-side">
          <div className="fm-form-card">
            <div className="fm-form-mark">
              <span className="fm-form-mark-badge">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M4 12c2-4 4-6 8-6s6 2 8 6c-2 4-4 6-8 6s-6-2-8-6Z" />
                  <circle cx="12" cy="12" r="2" fill="currentColor" stroke="none" />
                </svg>
              </span>
              Fit<span>Manager</span>
            </div>

            <h1 className="fm-form-title">Welcome back</h1>

            {error && <div className="fm-error">{error}</div>}

            <div className="fm-field">
              <label className="fm-label" htmlFor="fm-email">Email</label>
              <Input
                id="fm-email"
                prefix={
                  <svg className="fm-input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <rect x="2" y="4" width="20" height="16" rx="2" />
                    <path d="m22 6-10 7L2 6" />
                  </svg>
                }
                value={email}
                onChange={(event) => setEmail(event.target.value)}
                onKeyDown={handleKeyDown}
                placeholder="Enter your email address"
                autoComplete="email"
              />
            </div>

            <div className="fm-field">
              <label className="fm-label" htmlFor="fm-password">Password</label>
              <Input.Password
                id="fm-password"
                prefix={
                  <svg className="fm-input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <rect x="3" y="11" width="18" height="10" rx="2" />
                    <path d="M7 11V7a5 5 0 0 1 10 0v4" />
                  </svg>
                }
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                onKeyDown={handleKeyDown}
                placeholder="Enter your password"
                autoComplete="current-password"
              />
            </div>

            <Button
              type="primary"
              className="fm-submit"
              onClick={handleSubmit}
              loading={loading}
            >
              Log in
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <line x1="5" y1="12" x2="19" y2="12" />
                <polyline points="12 5 19 12 12 19" />
              </svg>
            </Button>

            <div className="fm-divider"><span>Or continue with</span></div>

            <div className="fm-social-row">
              <button type="button" className="fm-social-btn" onClick={handleGoogleLogin}>
                <svg viewBox="0 0 24 24" width="18" height="18">
                  <path fill="#EA4335" d="M12 10.2v3.9h5.5c-.24 1.3-1.7 3.8-5.5 3.8-3.3 0-6-2.7-6-6.1s2.7-6.1 6-6.1c1.9 0 3.15.8 3.88 1.5l2.65-2.55C16.9 3.05 14.66 2 12 2 6.98 2 2.9 6.02 2.9 11s4.08 9 9.1 9c5.25 0 8.73-3.7 8.73-8.9 0-.6-.07-1.05-.15-1.5H12Z" />
                </svg>
                Google
              </button>
            </div>

            <p className="fm-foot">Don't have an account? <a href="/signup">Sign up</a></p>
          </div>
        </div>

        <div className="fm-image-side">
          <div className="fm-image-glow" />
          <div className="fm-image-frame">
            <img
              src={heroAthlete}
              alt="Athlete training"
              className="fm-image-photo"
            />
            <div className="fm-image-overlay" />
            <div className="fm-image-caption">
              <p className="fm-image-kicker">FitManager</p>
              <h2 className="fm-image-headline">Train harder.<br />Track smarter.</h2>
            </div>
          </div>
        </div>
      </div>
    </ConfigProvider>
  );
}

export default LoginPage;
