import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import apiClient, { setAccessToken, setLogoutHandler } from '../api/axiosConfig';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [accessTokenState, setAccessTokenState] = useState(null);
  const [loading, setLoading] = useState(true);

  const logout = useCallback(() => {
    setUser(null);
    setAccessTokenState(null);
    setAccessToken(null);
    localStorage.removeItem('refreshToken');
  }, []);

  const login = useCallback((authData) => {
    const { accessToken, refreshToken, userId, email, role, firstName, lastName } = authData;

    setAccessTokenState(accessToken);
    setAccessToken(accessToken);

    if (refreshToken) {
      localStorage.setItem('refreshToken', refreshToken);
    }

    const userData = {
      userId: userId || null,
      email: email || '',
      role: role || 'ROLE_AUDITOR',
      firstName: firstName || '',
      lastName: lastName || '',
    };

    setUser(userData);
  }, []);

  // Initial session restoration on app load via refresh token
  useEffect(() => {
    setLogoutHandler(logout);

    const initAuth = async () => {
      const storedRefreshToken = localStorage.getItem('refreshToken');
      if (!storedRefreshToken) {
        setLoading(false);
        return;
      }

      try {
        const response = await apiClient.post('/api/auth/refresh', {
          refreshToken: storedRefreshToken,
        });

        const resData = response.data?.data || response.data;
        if (resData?.accessToken) {
          setAccessTokenState(resData.accessToken);
          setAccessToken(resData.accessToken);

          if (resData.refreshToken) {
            localStorage.setItem('refreshToken', resData.refreshToken);
          }

          setUser({
            userId: resData.userId || null,
            email: resData.email || '',
            role: resData.role || 'ROLE_AUDITOR',
            firstName: resData.firstName || '',
            lastName: resData.lastName || '',
          });
        } else {
          logout();
        }
      } catch {
        // Silent failure on init refresh — clear stored invalid refresh token
        logout();
      } finally {
        setLoading(false);
      }
    };

    initAuth();
  }, [logout]);

  const value = {
    user,
    accessToken: accessTokenState,
    role: user?.role || null,
    isAuthenticated: Boolean(user && accessTokenState),
    loading,
    login,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}

export default AuthContext;
