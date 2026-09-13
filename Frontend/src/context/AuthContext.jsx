import { createContext, useContext, useState } from 'react';

const AuthContext = createContext(null);

const TOKEN_KEY = 'fitmanager_token';

const decodeTokenPayload = (token) => {
  if (!token) {
    return null;
  }

  try {
    const payload = token.split('.')[1];

    return JSON.parse(
      atob(payload.replace(/-/g, '+').replace(/_/g, '/'))
    );
  } catch {
    return null;
  }
};

const getUserRole = (user) => {
  if (!user?.authorities) {
    return null;
  }

  if (user.authorities.includes('ROLE_ADMIN')) {
    return 'ADMIN';
  }

  if (user.authorities.includes('ROLE_STAFF')) {
    return 'STAFF';
  }

  return null;
};

export function AuthProvider({ children }) {
  const [token, setToken] = useState(
    () => localStorage.getItem(TOKEN_KEY)
  );

  const [user, setUser] = useState(
    () => decodeTokenPayload(
      localStorage.getItem(TOKEN_KEY)
    )
  );

  const login = (jwtToken) => {
    localStorage.setItem(TOKEN_KEY, jwtToken);
    setToken(jwtToken);
    setUser(decodeTokenPayload(jwtToken));
  };

  const logout = () => {
    localStorage.removeItem(TOKEN_KEY);
    setToken(null);
    setUser(null);
  };


  return (
    <AuthContext.Provider
      value={{
        token,
        user,
        role: getUserRole(user),
        login,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}