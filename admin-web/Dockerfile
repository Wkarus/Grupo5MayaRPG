# Build estático (Vite) + nginx para servir o painel admin no Docker
FROM node:20-bookworm-slim AS build
WORKDIR /app

COPY package*.json ./
RUN npm install

COPY . .

# URL da API vista pelo navegador no PC anfitrião (mapeamento 8081->container api)
ARG VITE_API_URL=http://localhost:8081
ENV VITE_API_URL=${VITE_API_URL}

RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
