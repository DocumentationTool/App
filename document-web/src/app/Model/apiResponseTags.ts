export interface Tag {
  tagId: { id: string };
  tagName: string;
}

export interface ApiResponseTags {
  message: string;
  error: string;
  content: Tag[];
}
