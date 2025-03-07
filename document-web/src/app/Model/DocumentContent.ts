export interface DocumentContent {
  path: string;
  repoId: string;
  createdBy: string;
  createdAt: string;
  category: string;
  lastModifiedBy: string;
  lastModifiedAt: string;
  isEditable: boolean;
  content: string;
}

export interface ApiResponse {
  message: string;
  error: string;
  content: DocumentContent[];
}
