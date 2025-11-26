import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import { Booking } from '../../types';

interface BookingState {
  bookings: Booking[];
  currentBooking: Booking | null;
}

const initialState: BookingState = {
  bookings: [],
  currentBooking: null,
};

const bookingSlice = createSlice({
  name: 'booking',
  initialState,
  reducers: {
    addBooking: (state, action: PayloadAction<Booking>) => {
      state.bookings.push(action.payload);
    },
    setCurrentBooking: (state, action: PayloadAction<Booking | null>) => {
      state.currentBooking = action.payload;
    },
  },
});

export const { addBooking, setCurrentBooking } = bookingSlice.actions;
export default bookingSlice.reducer;