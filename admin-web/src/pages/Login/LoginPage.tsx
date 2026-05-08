import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { useAuth } from "../../hooks/useAuth";

const schema = z.object({
  email: z.string().email("Email invalido"),
  senha: z.string().min(4, "Senha obrigatoria")
});

type LoginForm = z.infer<typeof schema>;

export function LoginPage() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [error, setError] = useState<string | null>(null);
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting }
  } = useForm<LoginForm>({ resolver: zodResolver(schema) });

  async function onSubmit(values: LoginForm) {
    try {
      setError(null);
      await login(values.email, values.senha);
      navigate("/dashboard");
    } catch {
      setError("Falha no login.");
    }
  }

  return (
    <div style={{ maxWidth: 360, margin: "60px auto", fontFamily: "Arial, sans-serif" }}>
      <h1>Login Admin</h1>
      <form onSubmit={handleSubmit(onSubmit)} style={{ display: "flex", flexDirection: "column", gap: 12 }}>
        <input placeholder="Email" {...register("email")} />
        {errors.email && <small>{errors.email.message}</small>}
        <input placeholder="Senha" type="password" {...register("senha")} />
        {errors.senha && <small>{errors.senha.message}</small>}
        {error && <small>{error}</small>}
        <button disabled={isSubmitting} type="submit">
          {isSubmitting ? "Entrando..." : "Entrar"}
        </button>
      </form>
    </div>
  );
}
