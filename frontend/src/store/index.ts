import { configureStore } from '@reduxjs/toolkit';
import authSlice from './slices/authSlice';
import searchSlice from './slices/searchSlice';
import bookingSlice from './slices/bookingSlice';

export const store = configureStore({
  reducer: {
    auth: authSlice,
    search: searchSlice,
    booking: bookingSlice,
  },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;