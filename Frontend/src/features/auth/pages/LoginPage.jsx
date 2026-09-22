import { useState } from 'react';
import { Input, Button } from 'antd';
import { Link, useNavigate } from 'react-router-dom';
import { login as loginApi } from '../api/authApi';
import { API_BASE_URL } from '../../../api/axiosClient';
import AuthPageLayout from '../components/AuthPageLayout';
import { ArrowRightIcon, EmailIcon, PasswordIcon } from '../components/AuthIcons';
import { useAuth } from '../../../context/AuthContext.jsx';

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
    <AuthPageLayout>
      <h1 className="fm-form-title">Welcome back</h1>

      {error && <div className="fm-error">{error}</div>}

      <div className="fm-field">
        <label className="fm-label" htmlFor="fm-email">Email</label>
        <Input
          id="fm-email"
          prefix={<EmailIcon />}
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
          prefix={<PasswordIcon />}
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
        <ArrowRightIcon />
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

      <p className="fm-foot">Don't have an account? <Link to="/signup">Sign up</Link></p>
    </AuthPageLayout>
  );
}

export default LoginPage;
