export interface ApiResponseTags {
  message: string | null;
  error: string | null;
  content: Record<string, string>;
}
