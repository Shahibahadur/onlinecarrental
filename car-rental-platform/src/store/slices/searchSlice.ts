import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import { SearchFilters } from '../../types';

const initialState: SearchFilters = {
  location: '',
  startDate: '',
  endDate: '',
  carType: '',
  minPrice: 0,
  maxPrice: 1000,
  transmission: '',
  fuelType: '',
};

const searchSlice = createSlice({
  name: 'search',
  initialState,
  reducers: {
    setFilters: (state, action: PayloadAction<Partial<SearchFilters>>) => {
      return { ...state, ...action.payload };
    },
    clearFilters: () => initialState,
  },
});

export const { setFilters, clearFilters } = searchSlice.actions;
export default searchSlice.reducer;