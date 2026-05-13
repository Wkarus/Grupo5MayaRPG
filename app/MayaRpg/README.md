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

**3.** Crie o arquivo `local.properties` na raiz do projeto (se não existir) e adicione:

```properties
# Emulador apontando para o PC (Docker na porta 8081)
MAYA_API_BASE_URL=http://10.0.2.2:8081/
```

**4.** Clique em **Run** no emulador ou dispositivo físico.

---

## 📦 Gerar APK para outra pessoa

O APK grava a URL da API no momento do build. O endereço `10.0.2.2` **só funciona no emulador** — não alcança o servidor de fora.

**Antes de gerar o APK**, defina o IP/URL real em `local.properties`:

```properties
# Mesma rede Wi-Fi: use o IP do PC (veja com ipconfig)
MAYA_API_BASE_URL_RELEASE=http://192.168.X.X:8081/

# Servidor público (recomendado para distribuição)
MAYA_API_BASE_URL_RELEASE=https://sua-api-publica.com/
```

Depois: **Build → Generate Signed Bundle / APK** no Android Studio.

> ⚠️ A API precisa estar online e acessível para quem instalar o app.

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
