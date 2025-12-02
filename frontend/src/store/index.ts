import { configureStore } from '@reduxjs/toolkit';
import authReducer from './auth.store';
import searchReducer from './search.store';
import bookingReducer from './booking.store';

export const store = configureStore({
  reducer: {
    auth: authReducer,
    search: searchReducer,
    booking: bookingReducer,
  },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;