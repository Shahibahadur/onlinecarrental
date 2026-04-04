import type { User } from '../types/auth';

export const AUTH_USER_KEY = 'carRentalAuthUser';

export function readPersistedUser(): User | null {
  const raw = localStorage.getItem(AUTH_USER_KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw) as User;
  } catch {
    return null;
  }
}

/** Backend JWT uses `sub` = email; no role/name in token — use persisted user when possible */
export function parseUserFromJwt(token: string): User | null {
  try {
    const parts = token.split('.');
    if (parts.length < 2) return null;
    const payload = JSON.parse(atob(parts[1])) as { sub?: string; exp?: number };
    const sub = payload.sub;
    if (!sub) return null;
    return {
      id: sub,
      email: sub,
      firstName: '',
      lastName: '',
      phone: '',
    };
  } catch {
    return null;
  }
}

export function clearAllAuthStorage(): void {
  localStorage.removeItem('authToken');
  localStorage.removeItem(AUTH_USER_KEY);
}
