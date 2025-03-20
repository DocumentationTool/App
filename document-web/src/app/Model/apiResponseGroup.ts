export interface Group {
  groupId: string;
  name: string;
  permissions: any[]; // Falls später genauer definiert, ersetzen
  users: any[]; // Falls später genauer definiert, ersetzen
}

export interface ApiResponseGroup {
  message: string | null;
  error: string | null;
  content: Group[];
}
