export interface ApiResponseModelRepos {
  message: string;
  error: string;
  content: Repos[];
}

export interface Repos {
  id: string;
  path: string;
  dbName: string;
  dbStorage: string;
  isReadOnly: boolean;
}
