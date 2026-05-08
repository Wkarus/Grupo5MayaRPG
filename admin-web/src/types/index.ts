export type UserRole = "ADMIN" | "USER";

export interface AuthUser {
  id: number;
  email: string;
  nome?: string;
  role: UserRole;
}
