# >_KTAR

⚡ **KTAR  in a connection.**

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Release](https://img.shields.io/github/v/release/felipemacedo1/ktar)](https://github.com/felipemacedo1/ktar/releases)
[![Downloads](https://img.shields.io/github/downloads/felipemacedo1/ktar/total)](https://github.com/felipemacedo1/ktar/releases)

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=felipemacedo1_ktar&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=felipemacedo1_ktar)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=felipemacedo1_ktar&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=felipemacedo1_ktar)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=felipemacedo1_ktar&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=felipemacedo1_ktar)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=felipemacedo1_ktar&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=felipemacedo1_ktar)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=felipemacedo1_ktar&metric=coverage)](https://sonarcloud.io/summary/new_code?id=felipemacedo1_ktar)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=felipemacedo1_ktar&metric=bugs)](https://sonarcloud.io/summary/new_code?id=felipemacedo1_ktar)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=felipemacedo1_ktar&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=felipemacedo1_ktar)

Um aplicativo Android profissional e seguro para conexão SSH a servidores remotos, com interface moderna em Jetpack Compose e autenticação robusta.

## 🚀 Características

- ✅ **Conexões SSH seguras** usando [SSHJ](https://github.com/hierynomus/sshj)
- ✅ **Autenticação múltipla**: senha ou chave pública (RSA/ED25519)
- ✅ **Terminal SSH real** com shell persistente e PTY
- ✅ **Output em tempo real** via streaming adaptativo
- ✅ **Estado persistente** - comandos cd, export, variáveis mantidos
- ✅ **Comandos longos** - tail -f, top -b, watch funcionam
- ✅ **Persistência de hosts** com DataStore
- ✅ **Criptografia de credenciais** via Android Keystore (AES-GCM)
- ✅ **Interface Material 3** com tema dark/light
- ✅ **Suporte a múltiplas sessões**
- ✅ **Host key verification** (Trust On First Use)

## 📥 Installation

KTAR requires Android 8.0 (API 26) or higher.

**Option 1: Download Pre-built APK** (Easiest)
- Go to [Releases](https://github.com/felipemacedo1/ktar/releases)
- Download the latest `app-debug.apk`
- Install on your Android device

**Option 2: Build from Source**
- See [DEVELOPMENT.md](DEVELOPMENT.md) for detailed build instructions

## 🚀 Getting Started

1. **Install KTAR** on your Android device
2. **Open the app** and grant required permissions
3. **Add a connection**:
   - Tap the "+" button
   - Enter SSH server details (host, port, username)
   - Choose authentication method (password or key)
4. **Connect** and start using the terminal!

For detailed setup, see [docs/INSTALL.md](docs/INSTALL.md)

## 📚 Documentation

- 📖 **[Installation Guide](docs/INSTALL.md)** - Detailed setup instructions
- 🔧 **[Development Guide](DEVELOPMENT.md)** - Build from source
- 🏗️ **[Architecture](ARCHITECTURE.md)** - Technical overview
- 🤝 **[Contributing](CONTRIBUTING.md)** - How to contribute
- ❓ **[FAQ](docs/FAQ.md)** - Common questions
- 🔄 **[Migration Guide](docs/MIGRATION.md)** - Version upgrade guide
- 🔐 **[Security Policy](SECURITY.md)** - Security & vulnerabilities

## 🛠️ Stack Tecnológica

| Componente | Tecnologia |
|-----------|-----------|
| Linguagem | Kotlin |
| UI Framework | Jetpack Compose + Material 3 |
| Arquitetura | MVVM + Clean Architecture |
| SSH Client | SSHJ 0.38.0 |
| Criptografia | Android Keystore + BouncyCastle |
| Persistência | Jetpack DataStore |
| Async | Kotlin Coroutines + Flow |
| Navegação | Jetpack Navigation Compose |
| Testes | JUnit 4 + MockK + Espresso |

## 📋 Requisitos

- **Android SDK**: 26+ (Android 8.0 Oreo)
- **Target SDK**: 35 (Android 15)
- **Android Studio**: Hedgehog (2023.1.1) ou superior
- **JDK**: 17 ou superior
- **Gradle**: 8.2+

## 🏗️ Estrutura do Projeto

```
app/src/main/java/com/ktar/
├── data/
│   ├── datastore/          # DataStore para persistência
│   │   └── HostDataStore.kt
│   ├── model/              # Models de dados
│   │   ├── Host.kt
│   │   ├── CommandResult.kt
│   │   └── ConnectionLog.kt
│   └── security/           # Gerenciamento de segurança
│       └── SecurityManager.kt
├── ssh/                    # Lógica SSH
│   ├── SSHManager.kt
│   └── SSHSession.kt
├── ui/
│   ├── components/         # Componentes reutilizáveis
│   │   ├── Dialogs.kt
│   │   └── HostCard.kt
│   ├── screens/           # Telas do app
│   │   ├── connection/    # Tela de conexão
│   │   ├── hostlist/      # Lista de hosts
│   │   └── terminal/      # Terminal SSH
│   └── theme/             # Tema Material 3
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
└── MainActivity.kt
```

## 📱 Download e Instalação

### Para Usuários Finais

**📥 [Baixar APK](https://github.com/felipemacedo1/ktar/releases/latest)**

1. Baixe o arquivo `app-debug.apk` da página de releases
2. Habilite "Fontes desconhecidas" nas configurações do Android
3. Instale o APK baixado
4. Veja o [guia completo de instalação](INSTALL.md) para mais detalhes

### Para Desenvolvedores

1. Clone o repositório:
```bash
git clone https://github.com/felipemacedo1/ktar.git
cd ktar
```

2. Abra o projeto no Android Studio
3. Sincronize as dependências do Gradle
4. Execute o app em um dispositivo ou emulador Android

## 🚦 Como Usar

### Primeiro Uso

1. **Adicionar uma conexão SSH**:
   - Toque no botão "+" na tela inicial
   - Preencha os dados do servidor (host, porta, usuário)
   - Escolha o método de autenticação (senha ou chave pública)
   - Salve a conexão

2. **Conectar a um servidor**:
   - Selecione um host salvo na lista
   - Toque em "Conectar"
   - Aguarde a autenticação

3. **Usar o terminal**:
   - Digite comandos no prompt
   - Pressione Enter ou o botão de envio
   - Visualize a saída em tempo real
   - Digite `exit` para desconectar

## 🔐 Segurança

Este aplicativo implementa diversas camadas de segurança:

- **Criptografia de credenciais**: Senhas e chaves privadas são criptografadas usando AES-256-GCM via Android Keystore
- **Host key verification**: TOFU (Trust On First Use) para prevenir ataques MITM
- **Timeout de conexão**: 10 segundos para evitar travamentos
- **Sem logs de credenciais**: Senhas/chaves nunca são registradas em logs
- **ProGuard**: Ofuscação de código na versão release

## 🔄 Automações CI/CD

### SonarCloud Issues Sync

O projeto inclui sincronização automática de issues do SonarCloud para GitHub Issues:

- **Execução automática**: Toda segunda-feira às 12:00 UTC
- **Execução manual**: Via GitHub Actions (workflow `sonar-sync.yml`)
- **Filtros personalizáveis**: Por severidade e tipo de issue
- **Deduplicação inteligente**: Evita issues duplicadas
- **Labels automáticas**: Aplicadas por tipo e prioridade

**Configuração rápida:**
```bash
# Setup inicial (cria labels e valida setup)
./scripts/setup_sonar_sync.sh

# Execução manual local
export SONAR_TOKEN="your-token"
./scripts/sync_sonar_issues.sh

# Dry run (preview sem criar issues)
./scripts/sync_sonar_issues.sh --dry-run
```

📚 **Documentação completa**: [docs/SONARCLOUD_SYNC.md](docs/SONARCLOUD_SYNC.md)

---

## 🧪 Testes

Execute os testes unitários:

```bash
./gradlew test
```

Execute os testes instrumentados (requer dispositivo/emulador):

```bash
./gradlew connectedAndroidTest
```

## 📦 Build

### Debug APK

```bash
./gradlew assembleDebug
```

O APK será gerado em: `app/build/outputs/apk/debug/app-debug.apk`

### Release APK (Assinado)

1. Configure o keystore em `gradle.properties` (não commitado):
```properties
KEYSTORE_FILE=/path/to/keystore.jks
KEYSTORE_PASSWORD=your_password
KEY_ALIAS=your_alias
KEY_PASSWORD=your_key_password
```

2. Build:
```bash
./gradlew assembleRelease
```

## 🤝 Contribuindo

Contribuições são bem-vindas! Por favor:

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças usando Conventional Commits (`git commit -m 'feat: Add amazing feature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

### Conventional Commits

Este projeto usa [Conventional Commits](https://www.conventionalcommits.org/):

- `feat:` Nova funcionalidade
- `fix:` Correção de bug
- `docs:` Documentação
- `refactor:` Refatoração de código
- `test:` Adição de testes
- `chore:` Tarefas de manutenção

## 📝 Roadmap

### 🚧 v1.0.0-beta1 - Current Beta Release
- ✅ Core SSH functionality
- ✅ Terminal with PTY support
- ✅ Host management with encryption
- ✅ Material 3 UI
- 🔄 Security audit in progress
- 🔄 Bug fixes and stability improvements

### 🎯 v1.0.0 - Official Stable Release (When Ready)
**Critérios para lançamento:**
- [ ] ✅ **Segurança**: Todas vulnerabilidades críticas corrigidas
- [ ] ✅ **Funcionalidade**: Todas features principais estáveis
- [ ] ✅ **Qualidade**: Zero bugs críticos conhecidos
- [ ] ✅ **Testes**: Cobertura de testes adequada
- [ ] ✅ **Performance**: Otimizações implementadas
- [ ] ✅ **Documentação**: Completa e atualizada

**A versão oficial será lançada apenas quando a aplicação estiver completamente segura, funcional e livre de bugs.**

#### v1.1.0 - Enhanced Features (Planned)
- [ ] Suporte a setas ↑↓ (histórico de comandos)
- [ ] Backspace e edição de linha
- [ ] Ctrl+C para interromper comandos
- [ ] Tab completion

#### v1.2.0 - Advanced Terminal (Planned)
- [ ] ANSI color support
- [ ] Command history with arrow keys
- [ ] Tab completion
- [ ] Ctrl+C interrupt support

#### v2.0.0 - Professional Features (Vision)
- [ ] SFTP file transfer
- [ ] SSH tunneling
- [ ] Multiple sessions/tabs
- [ ] Session recording

### Outras Features
- [ ] Suporte a SFTP para transferência de arquivos
- [ ] SSH tunneling (port forwarding)
- [ ] Exportar/importar configurações
- [ ] Widget para acesso rápido
- [ ] Temas customizáveis

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

## 👤 Autor

**Felipe Macedo**

- GitHub: [@felipemacedo1](https://github.com/felipemacedo1)

## 🙏 Agradecimentos

- [SSHJ](https://github.com/hierynomus/sshj) - Biblioteca SSH para Java
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - Toolkit moderno de UI
- [Material Design 3](https://m3.material.io/) - Sistema de design do Google

---

**⚠️ Aviso Beta**: Esta é uma versão beta em desenvolvimento ativo. Embora funcional, pode conter bugs e vulnerabilidades. A versão oficial (v1.0.0) será lançada apenas após auditoria completa de segurança e correção de todos os problemas identificados. Use por sua conta e risco.
