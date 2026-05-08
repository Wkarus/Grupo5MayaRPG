import type { NextFunction, Request, Response } from "express";
import { ZodError } from "zod";
import { ApiError } from "../utils/ApiError";

export function notFoundHandler(_req: Request, res: Response) {
  res.status(404).json({ message: "Rota nao encontrada." });
}

export function errorHandler(err: unknown, _req: Request, res: Response, _next: NextFunction) {
  if (err instanceof ZodError) {
    return res.status(400).json({ message: "Payload invalido.", errors: err.flatten().fieldErrors });
  }

  if (err instanceof ApiError) {
    return res.status(err.statusCode).json({
      message: err.message,
      ...(err.code != null ? { code: err.code } : {})
    });
  }

  console.error(err);
  return res.status(500).json({ message: "Erro interno do servidor." });
}
