import {Resources} from './apiResponseFileTree';

export interface ApiResponseResource{
  message: string;
  error: string;
  content: Record<string, Resources[]>;
}

