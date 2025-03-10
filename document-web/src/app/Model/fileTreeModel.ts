export interface ApiResponse {
  message: string;
  error: string;
  content: Record<string, ContentGroup>;
}

export interface ContentGroup {
  resources: Resource[];
}

export interface Resource {
  path: string;
  repoId: string;
  createdBy: string;
  createdAt: string;
  category: string;
  tags: Record<string, string>; // Beliebige Key-Value-Paare
  lastModifiedBy: string;
  lastModifiedAt: string;
  isEditable: boolean;
  data: string;
}
