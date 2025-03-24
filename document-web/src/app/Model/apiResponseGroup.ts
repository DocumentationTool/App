export interface Group {
  groupId: string;
  name: string;
  permissions: any[];
  users: any[];
}

export interface ApiResponseGroup {
  message: string | null;
  error: string | null;
  content: Group[];
}
