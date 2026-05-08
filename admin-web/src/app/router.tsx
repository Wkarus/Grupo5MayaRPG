import { createBrowserRouter, Navigate } from "react-router-dom";
import { PrivateRoute } from "../components/auth/PrivateRoute";
import { AdminLayout } from "../components/layout/AdminLayout";
import { AgendaPage } from "../pages/Agenda/AgendaPage";
import { ComentariosPage } from "../pages/Comentarios/ComentariosPage";
import { DashboardPage } from "../pages/Dashboard/DashboardPage";
import { LoginPage } from "../pages/Login/LoginPage";
import { PostsPage } from "../pages/Posts/PostsPage";

export const router = createBrowserRouter([
  { path: "/login", element: <LoginPage /> },
  {
    element: <PrivateRoute />,
    children: [
      {
        element: <AdminLayout />,
        children: [
          { path: "/", element: <Navigate to="/dashboard" replace /> },
          { path: "/dashboard", element: <DashboardPage /> },
          { path: "/posts", element: <PostsPage /> },
          { path: "/agenda", element: <AgendaPage /> },
          { path: "/comentarios", element: <ComentariosPage /> }
        ]
      }
    ]
  }
]);
