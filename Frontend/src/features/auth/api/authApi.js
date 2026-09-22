import axiosClient from '../../../api/axiosClient';

export const register = (registerRequest) => {
  return axiosClient.post('/api/v1/auth/register', registerRequest);
};

export const login = (loginRequest) => {
  return axiosClient.post('/api/v1/auth/login', loginRequest);
};

export const exchangeOAuth2Code = (code) => {
  return axiosClient.post('/api/v1/auth/oauth2/exchange', { code });
};
