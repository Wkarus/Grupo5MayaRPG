# 📱 MayaRpg — App Android

App mobile do projeto MayaRpg (fisioterapia + RPG), desenvolvido em Java para Android.

---

## ✅ Funcionalidades

| Funcionalidade | Descrição |
|---|---|
| 🔐 Autenticação | Login com Firebase (e-mail + senha) + JWT para a API |
| 🏋️ Exercícios | Lista vinda da API REST (`GET /exercises`) |
| ✅ Check-in | Registra execução do exercício (`POST /exercises/:id/checkin`) |
| 💾 Histórico local | Check-ins salvos no dispositivo (SharedPreferences) |
| 👤 Área do usuário | Visualiza histórico de exercícios e consultas agendadas |
| 📅 Agenda | Agendamento de consultas integrado ao backend |
| 🔔 Notificações | Alerta quando há exercícios novos na API |

---

## 🛠️ Pré-requisitos

- [Android Studio](https://developer.android.com/studio)
- JDK 11+
- Projeto Firebase configurado (Authentication + Firestore)
- Arquivo `google-services.json` dentro de `app/`

---

## ▶️ Como rodar

**1.** Abra a pasta `app/MayaRpg` no Android Studio.

**2.** Aguarde o Gradle sync.

**3.** Crie o arquivo `local.properties` na raiz de `app/MayaRpg` (copie de `local.properties.example` se precisar) e configure a URL da API — veja a seção [URL da API (`MAYA_API_BASE_URL`)](#-url-da-api-maya_api_base_url) abaixo.

**4.** Com a API rodando (`docker compose up -d` na raiz do repositório), clique em **Run** no emulador ou dispositivo físico.

**5.** Depois de mudar qualquer URL em `local.properties`, faça **Build → Rebuild Project** antes de instalar de novo no celular.

---

## 🌐 URL da API (`MAYA_API_BASE_URL`)

O app **não escolhe a API sozinho**: a URL é gravada no build a partir de `local.properties` e vira `BuildConfig.API_BASE_URL` (usado pelo Retrofit em `network/ApiClient.java`).

| Propriedade | Quando usa |
|-------------|------------|
| `MAYA_API_BASE_URL` | Build **debug** (Run no Android Studio) |
| `MAYA_API_BASE_URL_RELEASE` | Build **release** (APK assinado) |

Use **barra no final** (`/`). Exemplo: `https://exemplo.trycloudflare.com/`

A API deste repositório, com Docker, fica em **`http://localhost:8081`** no PC (container na porta 8080).

### Por que só funcionava no mesmo Wi‑Fi?

Endereços como `http://192.168.x.x:8081/` são **IP da rede de casa**. O celular em 4G ou em outro Wi‑Fi **não alcança** esse IP. O emulador usa `10.0.2.2` — isso **só vale dentro do emulador**.

Para usar o app **fora da sua rede**, a API precisa de um endereço **público na internet**. Opções:

1. **Cloudflare Tunnel** (grátis, bom para testes) — expõe o Docker do seu PC sem abrir porta no roteador.
2. **Mesmo Wi‑Fi** — IP local do PC (`ipconfig`).
3. **Servidor na nuvem** — URL fixa para produção ou entrega do APK.

### Opção A — Emulador (desenvolvimento)

```properties
MAYA_API_BASE_URL=http://10.0.2.2:8081/
```

`10.0.2.2` é o “localhost” do PC visto pelo emulador Android.

### Opção B — Celular na mesma rede Wi‑Fi

```properties
MAYA_API_BASE_URL=http://192.168.0.XXX:8081/
```

Substitua `XXX` pelo IPv4 do PC (`ipconfig` → Wi‑Fi). PC e celular na **mesma rede**.

### Opção C — Celular em 4G ou outro Wi‑Fi (Cloudflare Tunnel)

Com `docker compose up -d` rodando e a API em `8081`:

**1.** Instale o [cloudflared](https://developers.cloudflare.com/cloudflare-one/connections/connect-networks/downloads/) (Windows 64-bit: `cloudflared-windows-amd64.exe`).

**2.** No PowerShell, **na pasta do executável** (não dê duplo clique no `.exe`):

```powershell
cd "C:\caminho\para\Cloudflare"
.\cloudflared-windows-amd64.exe tunnel --url http://localhost:8081
```

**3.** Deixe a janela **aberta**. Copie a URL que aparecer, por exemplo:

```text
https://reflect-governing-engine-unknown.trycloudflare.com
```

**4.** Em `local.properties`:

```properties
MAYA_API_BASE_URL=https://SUA-URL.trycloudflare.com/
MAYA_API_BASE_URL_RELEASE=https://SUA-URL.trycloudflare.com/
```

**5.** **Build → Rebuild Project** e instale o app de novo.

**6.** Teste no PC: `https://SUA-URL.trycloudflare.com/health` → deve retornar `{"status":"ok"}`.

| Importante | Detalhe |
|------------|---------|
| PC ligado | Docker + túnel precisam estar rodando enquanto usa o app |
| URL temporária | Túnel rápido (`trycloudflare.com`) **muda** se fechar o PowerShell; atualize `local.properties` e faça rebuild |
| Banco de dados | Continua no MySQL do Docker no seu PC; o túnel só encaminha tráfego para a API |
| Código novo | Mudou backend → `docker compose up -d --build`; mudou app → rebuild no Android Studio |

Conta Cloudflare no plano **Free** é gratuita para túnel; não é obrigatório comprar domínio para esse teste rápido.

---

## 📦 Gerar APK para outra pessoa

O APK grava a URL no momento do build. `10.0.2.2` **só funciona no emulador**.

Defina `MAYA_API_BASE_URL_RELEASE` com uma URL que o celular alcance de qualquer rede:

```properties
# Túnel Cloudflare (testes; URL muda ao reiniciar o túnel)
MAYA_API_BASE_URL_RELEASE=https://SUA-URL.trycloudflare.com/

# Ou servidor público fixo (recomendado para distribuição)
MAYA_API_BASE_URL_RELEASE=https://sua-api-publica.com/
```

Depois: **Build → Generate Signed Bundle / APK**.

> ⚠️ A API (e o túnel, se usar Cloudflare) precisam estar online para quem instalar o app funcionar.

---

## 🔄 Fluxo principal

```
Login (Firebase)
    ↓
Tela de Exercícios → GET /exercises (API)
    ↓
Seleciona exercício → Tela de detalhe
    ↓
Botão "Registrar execução" → POST /exercises/:id/checkin (API)
    ↓
Salva localmente (SharedPreferences)
    ↓
Aba Usuário → Histórico de exercícios feitos
```

---

## 📁 Estrutura relevante

```
app/src/main/java/com/example/mayarpg/
├── MainActivity.java           # Login
├── ExercisesFragment.java      # Lista de exercícios (API)
├── ExerciseDetailFragment.java # Detalhe + check-in
├── ExerciseHistoryStore.java   # Persistência local
├── ExerciseNewNotification.java # Notificações
├── UserFragment.java           # Histórico do usuário
└── network/
    ├── ApiClient.java          # Retrofit + autenticação
    └── services/
        └── ExerciseService.java
```

---

## 🔗 Dependências principais

- Firebase Authentication + Firestore
- Retrofit 2 (chamadas REST)
- OkHttp (interceptor de autenticação JWT)
