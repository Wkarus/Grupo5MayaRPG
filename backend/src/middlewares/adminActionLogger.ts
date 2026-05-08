import type { NextFunction, Request, Response } from "express";

export function adminActionLogger(actionName: string) {
  return (req: Request, _res: Response, next: NextFunction) => {
    if (req.user?.role === "ADMIN") {
      console.info(
        JSON.stringify({
          type: "ADMIN_ACTION",
          action: actionName,
          adminId: req.user.id,
          method: req.method,
          path: req.path,
          timestamp: new Date().toISOString()
        })
      );
    }
    next();
  };
}
