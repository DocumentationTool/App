import {DocumentContentResponseModel} from './DocumentContentResponseModel';

export interface ApiResponseModel {
  message: string;
  error: string;
  content: DocumentContentResponseModel[];
}
