export interface ApiResponseModelResourceBeingEdited {
  message: string | null;
  error: string | null;
  content: {
    isBeingEdited: boolean;
    editingUser: string | null;
    file: string;
  };
}
