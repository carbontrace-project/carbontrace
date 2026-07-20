import axios from 'axios';

const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

// In-memory access token storage (never written to localStorage)
let accessToken = null;
let onLogoutCallback = null;

export const setAccessToken = (token) => {
  accessToken = token;
};

export const getAccessToken = () => {
  return accessToken;
};

export const setLogoutHandler = (callback) => {
  onLogoutCallback = callback;
};

const apiClient = axios.create({
  baseURL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
});

// Request queueing mechanism for concurrent 401s during token refresh
let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
  failedQueue.forEach((promise) => {
    if (error) {
      promise.reject(error);
    } else {
      promise.resolve(token);
    }
  });
  failedQueue = [];
};

// Request Interceptor: Attach Bearer token from memory if present
apiClient.interceptors.request.use(
  (config) => {
    if (accessToken && !config.headers.Authorization && !config.skipAuth) {
      config.headers.Authorization = `Bearer ${accessToken}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response Interceptor: 401 Unauthorized token refresh & retry
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // Only intercept 401 Unauthorized responses
    if (!error.response || error.response.status !== 401) {
      return Promise.reject(error);
    }

    // Do not attempt refresh on auth endpoints or if already retried
    const isAuthEndpoint =
      originalRequest.url?.includes('/api/auth/refresh') ||
      originalRequest.url?.includes('/api/auth/login');

    if (originalRequest._retry || isAuthEndpoint) {
      if (onLogoutCallback && !originalRequest.url?.includes('/api/auth/login')) {
        onLogoutCallback();
      }
      return Promise.reject(error);
    }

    // Queue concurrent requests if a refresh is already in progress
    if (isRefreshing) {
      return new Promise((resolve, reject) => {
        failedQueue.push({ resolve, reject });
      })
        .then((token) => {
          originalRequest.headers.Authorization = `Bearer ${token}`;
          return apiClient(originalRequest);
        })
        .catch((err) => Promise.reject(err));
    }

    originalRequest._retry = true;
    isRefreshing = true;

    const storedRefreshToken = localStorage.getItem('refreshToken');
    if (!storedRefreshToken) {
      isRefreshing = false;
      if (onLogoutCallback) {
        onLogoutCallback();
      }
      return Promise.reject(error);
    }

    try {
      // Direct call to avoid interceptor recursion
      const response = await axios.post(
        `${baseURL}/api/auth/refresh`,
        { refreshToken: storedRefreshToken },
        {
          headers: { 'Content-Type': 'application/json' },
          withCredentials: true,
        }
      );

      const responseData = response.data?.data || response.data;
      const newAccessToken = responseData?.accessToken;
      const newRefreshToken = responseData?.refreshToken;

      if (!newAccessToken) {
        throw new Error('Refresh response did not return an access token.');
      }

      setAccessToken(newAccessToken);
      if (newRefreshToken) {
        localStorage.setItem('refreshToken', newRefreshToken);
      }

      processQueue(null, newAccessToken);

      originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
      return apiClient(originalRequest);
    } catch (refreshError) {
      processQueue(refreshError, null);
      localStorage.removeItem('refreshToken');
      setAccessToken(null);
      if (onLogoutCallback) {
        onLogoutCallback();
      }
      return Promise.reject(refreshError);
    } finally {
      isRefreshing = false;
    }
  }
);

export default apiClient;
