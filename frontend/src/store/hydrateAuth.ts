import type { Store } from '@reduxjs/toolkit';
import { setUser } from './slices/authSlice';
import { parseUserFromJwt, readPersistedUser } from '../utils/authStorage';

/**
 * Run before React mounts so the first paint already knows if the user is logged in.
 */
export function hydrateAuthFromStorage(store: Store): void {
  const token = localStorage.getItem('authToken');
  if (!token) return;

  let user = readPersistedUser();
  if (!user) {
    user = parseUserFromJwt(token);
  }
  if (user) {
    store.dispatch(setUser(user));
  }
}
