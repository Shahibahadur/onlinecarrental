import type { Middleware } from '@reduxjs/toolkit';
import { setUser, clearUser } from './slices/authSlice';
import { AUTH_USER_KEY, clearAllAuthStorage } from '../utils/authStorage';

/**
 * Keeps `authUser` in localStorage in sync with Redux, and clears token + user on logout.
 */
export const authPersistenceMiddleware: Middleware = () => (next) => (action) => {
  const result = next(action);

  if (setUser.match(action)) {
    try {
      localStorage.setItem(AUTH_USER_KEY, JSON.stringify(action.payload));
    } catch {
      /* ignore quota / private mode */
    }
  }

  if (clearUser.match(action)) {
    clearAllAuthStorage();
  }

  return result;
};
