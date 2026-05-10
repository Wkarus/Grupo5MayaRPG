# MayaRpg (Android)

Projeto Android do MayaRpg.

## Pré-requisitos

- Android Studio
- JDK 11+
- Firebase configurado (Auth e Firestore)
- arquivo `google-services.json` em `app/google-services.json`

## Como rodar

1. Abra `app/MayaRpg` no Android Studio.
2. Aguarde o Gradle sync.
3. Configure a URL da API em `local.properties`:

```properties
MAYA_API_BASE_URL=http://10.0.2.2:8081/
```

4. Rode no emulador/dispositivo.

## Fluxo principal implementado

- Tela de exercícios busca dados da API (`GET /exercises`)
- Registro de execução (`POST /exercises/:id/checkin`)
- Histórico local de exercícios (SharedPreferences)
- Histórico exibido na área do usuário

## Notificações (MVP)

Existe notificação local simples para exercícios novos:
- quando a contagem de exercícios da API aumenta, o app notifica.

## Observações

- Login no app usa Firebase.
- Cadastro de pacientes do admin fica no MySQL (não é o mesmo cadastro do Firebase).

