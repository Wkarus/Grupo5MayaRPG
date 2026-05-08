export type UserRole = "ADMIN" | "USER";

export interface AuthUser {
  id: number;
  email: string;
  role: UserRole;
  nome?: string;
}
