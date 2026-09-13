import { useState } from 'react';
import { Input, Button } from 'antd';
import { login as loginApi } from '../api/authApi';
import { useAuth } from '../context/AuthContext.jsx';

import { useNavigate } from 'react-router-dom';

function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = () => {
    const loginRequest = {
      email,
      password,
    };

    loginApi(loginRequest)
      .then((response) => {
        const loginResponse = response.data;
        login(loginResponse.token);
        navigate('/dashboard');
      })
      .catch((error) => {
        console.error('Login failed:', error);
      });
  };

  return (
    <div>
      <h1>FitManager</h1>

      <div>
        <label>Email</label>

        <Input
          value={email}
          onChange={(event) => setEmail(event.target.value)}
          placeholder="Enter your email"
        />
      </div>

      <div>
        <label>Password</label>

        <Input.Password
          value={password}
          onChange={(event) => setPassword(event.target.value)}
          placeholder="Enter your password"
        />
      </div>

      <Button
        type="primary"
        onClick={handleSubmit}
      >
        Login
      </Button>

    </div>
  );
}

export default LoginPage;