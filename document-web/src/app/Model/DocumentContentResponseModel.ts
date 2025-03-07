export interface DocumentContentResponseModel {
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
