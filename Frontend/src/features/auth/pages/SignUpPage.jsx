import { useState } from 'react';
import { Input, Button } from 'antd';
import { useNavigate, Link } from 'react-router-dom';
import { login as loginApi, register as registerApi } from '../api/authApi';
import AuthPageLayout from '../components/AuthPageLayout';
import { ArrowRightIcon, EmailIcon, PasswordIcon } from '../components/AuthIcons';
import { useAuth } from '../../../context/AuthContext.jsx';

function SignUpPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = () => {
    if (!email || !password || !confirmPassword) {
      setError('Please fill in all fields.');
      return;
    }
    if (password.length < 8) {
      setError('Password must be at least 8 characters.');
      return;
    }
    if (password !== confirmPassword) {
      setError('Passwords do not match.');
      return;
    }

    setError('');
    setLoading(true);

    registerApi({ email, password })
      .then(() => loginApi({ email, password }))
      .then((response) => {
        login(response.data.token);
        navigate('/dashboard');
      })
      .catch((err) => {
        setError(
          err?.response?.data?.message || 'Could not create your account. Please try again.'
        );
      })
      .finally(() => setLoading(false));
  };

  const handleKeyDown = (event) => {
    if (event.key === 'Enter') handleSubmit();
  };

  return (
    <AuthPageLayout>
      <h1 className="fm-form-title">Create your account</h1>

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
          placeholder="Create a password"
          autoComplete="new-password"
        />
      </div>

      <div className="fm-field">
        <label className="fm-label" htmlFor="fm-confirm-password">Re-enter password</label>
        <Input.Password
          id="fm-confirm-password"
          prefix={<PasswordIcon />}
          value={confirmPassword}
          onChange={(event) => setConfirmPassword(event.target.value)}
          onKeyDown={handleKeyDown}
          placeholder="Re-enter your password"
          autoComplete="new-password"
        />
      </div>

      <Button
        type="primary"
        className="fm-submit"
        onClick={handleSubmit}
        loading={loading}
      >
        Register
        <ArrowRightIcon />
      </Button>

      <p className="fm-foot">Already have an account? <Link to="/login">Log in</Link></p>
    </AuthPageLayout>
  );
}

export default SignUpPage;
