export interface ApiResponseFileTree {
  message: string;
  error: string;
  content: Record<string, ContentGroup>;
}

export interface ContentGroup {
  resources: Resources[];
  children: Record<string, ContentGroup>;
}

export interface Resources {
  path: string;
  repoId: string;
  createdBy: string;
  createdAt: string;
  category: string;
  tags: string[];
  lastModifiedBy: string;
  lastModifiedAt: string;
  permissionType: string;
  data: string;
}
