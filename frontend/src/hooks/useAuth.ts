import { useCallback } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import type { RootState } from '../store';
import { setUser, clearUser } from '../store/auth.store';
import type { User } from '../types/auth';

export const useAuth = () => {
  const dispatch = useDispatch();
  const auth = useSelector((state: RootState) => state.auth);

  const login = useCallback((user: User) => {
    dispatch(setUser(user));
  }, [dispatch]);

  const logout = useCallback(() => {
    dispatch(clearUser());
  }, [dispatch]);

  return {
    user: auth.user,
    isAuthenticated: auth.isAuthenticated,
    loading: auth.loading,
    login,
    logout,
  };
};

export default useAuth;
