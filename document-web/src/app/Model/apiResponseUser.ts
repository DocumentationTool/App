export interface Role {
  id: string;
  name: string;
}

export interface Permission {
  id: string;
  isGroup: boolean;
  isUser: boolean;
  path: string;
}

export interface User {
  userId: string;
  roles: Role[];
  permissions: Permission[];
  groups: string[];
}

export interface ApiResponseUser {
  message: string;
  error: string | null;
  content: User[];
}
