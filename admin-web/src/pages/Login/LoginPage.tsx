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
    <div style={{ maxWidth: 420, margin: "70px auto" }}>
      <div className="card">
        <h1 className="page-title" style={{ marginBottom: 4 }}>
          Login Admin
        </h1>
        <p className="page-subtitle">Use seu e-mail e senha para entrar no painel.</p>
        <form onSubmit={handleSubmit(onSubmit)} style={{ display: "flex", flexDirection: "column", gap: 12 }}>
        <input placeholder="Email" {...register("email")} />
        {errors.email && <small>{errors.email.message}</small>}
        <input placeholder="Senha" type="password" {...register("senha")} />
        {errors.senha && <small>{errors.senha.message}</small>}
        {error && <small>{error}</small>}
        <button className="btn btn-primary" disabled={isSubmitting} type="submit">
          {isSubmitting ? "Entrando..." : "Entrar"}
        </button>
        </form>
      </div>
    </div>
  );
}
