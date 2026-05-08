# imagem node
FROM node:20

# pasta interna container
WORKDIR /app

# copiar package
COPY package*.json ./

# instalar dependências
RUN npm install

# copiar restante projeto
COPY . .

# gerar prisma client
RUN npx prisma generate

# build nestjs
RUN npm run build

ENV PORT=8080

EXPOSE 8080

CMD ["node", "dist/src/main"]