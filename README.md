# Login Seguro

Sistema de autenticação e autorização desenvolvido com Java 21, Spring Boot, Spring Security, Thymeleaf e MongoDB Atlas. Permite cadastrar usuários, autenticar contas e controlar o acesso às páginas por perfil.

## Funcionalidades

- Login e logout.
- Cadastro de usuários por um administrador.
- Perfis de acesso: `ADMINISTRADOR`, `OPERADOR` e `PROFESSOR`.
- Validação dos dados, confirmação de senha e identificação de cadastros duplicados.
- Senhas armazenadas como hash BCrypt.
- Bloqueio da conta por 15 minutos após cinco tentativas de login com senha incorreta.
- Armazenamento de usuários e sessões HTTP no MongoDB Atlas.
- Expiração das sessões após 30 minutos de inatividade.
- Proteção CSRF nos formulários.
- Interface responsiva com cores e componentes de apresentação separados das regras de autenticação.

## Tecnologias

| Tecnologia | Uso |
| --- | --- |
| Java 21 | Linguagem e ambiente de execução |
| Spring Boot 4.0.8 | Configuração e execução da aplicação |
| Spring Security | Autenticação, autorização e proteção de senhas |
| Thymeleaf | Renderização das páginas HTML |
| Spring Data MongoDB | Persistência dos usuários |
| MongoDB Atlas | Banco de dados de usuários e sessões |
| Maven Wrapper | Gerenciamento de dependências e execução dos comandos de build |

## Pré-requisitos

- Git.
- JDK 21, disponível no terminal pelo comando `java -version`.
- Conta no MongoDB Atlas e um cluster configurado.
- Acesso à internet para conectar ao Atlas e baixar as dependências.

O Maven Wrapper acompanha o projeto, dispensando a instalação separada do Maven.

## Configuração e execução

### 1. Clonar o repositório

```bash
git clone https://github.com/Diego251Fagundes/sigee-login.git login-seguro
cd login-seguro
```

Execute os comandos seguintes na pasta que contém o arquivo `pom.xml`.

### 2. Configurar o MongoDB Atlas

1. Crie um cluster no Atlas.
2. Em **Database Access**, crie um usuário de banco com permissão de leitura e escrita no banco da aplicação, por exemplo, `login_seguro`.
3. Em **Network Access**, adicione o IP da rede onde a aplicação será executada.
4. Em **Connect > Drivers**, selecione Java e copie a string de conexão.
5. Preencha o usuário e a senha do banco e informe o nome do banco no caminho da URI.

O usuário de banco é usado para conectar ao Atlas. As contas de acesso à aplicação são cadastradas separadamente.

No PowerShell:

```powershell
$env:MONGODB_URI="mongodb+srv://<usuario-do-banco>:<senha-codificada>@<cluster>/login_seguro?retryWrites=true&w=majority"
```

Substitua os valores entre `< >` pelos dados do Atlas e utilize os parâmetros da URI fornecida pelo cluster. Caracteres reservados na senha do banco devem receber codificação percentual. A conexão `mongodb+srv` utiliza TLS por padrão.

A variável de ambiente deve ser definida no mesmo terminal usado para iniciar a aplicação. Ao abrir outro terminal, configure-a novamente. O nome do banco na URI determina a base de dados acessada.

O arquivo `src/main/resources/application.properties` lê a conexão por `MONGODB_URI`, sem credenciais gravadas no código. Sem essa variável, a aplicação procura um MongoDB local em `mongodb://localhost:27017/login_seguro`. Arquivos `.env` não são carregados automaticamente.

### 3. Configurar o primeiro administrador

Para uma instalação sem contas, defina os dados do administrador inicial antes de iniciar a aplicação. Se uma conta administrativa já estiver cadastrada, prossiga para a execução.

No PowerShell:

```powershell
$env:APP_ADMIN_INICIAL_ENABLED="true"
$env:APP_ADMIN_INICIAL_NOME="Administrador"
$env:APP_ADMIN_INICIAL_SOBRENOME="Sistema"
$env:APP_ADMIN_INICIAL_EMAIL="admin@example.com"
$env:APP_ADMIN_INICIAL_NOME_USUARIO="admin.login"
$env:APP_ADMIN_INICIAL_SENHA="substitua-por-uma-senha-segura"
```

Informe seus próprios dados e escolha uma senha com 8 a 72 caracteres, respeitando o limite de 72 bytes UTF-8 do BCrypt. A senha é convertida em hash antes de ser armazenada.

### 4. Iniciar a aplicação

No PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

Aguarde a mensagem `Started LoginSeguroApplication` no terminal e abra:

[http://localhost:8080/login](http://localhost:8080/login)

Entre com o nome de usuário e a senha definidos na etapa anterior. O administrador pode cadastrar novas contas e selecionar o perfil de cada usuário.

### 5. Encerrar e executar novamente

Para encerrar a aplicação, pressione `Ctrl+C` no terminal em que ela está rodando.

Depois da criação do primeiro administrador, desative a configuração inicial antes da próxima execução.

No PowerShell:

```powershell
$env:APP_ADMIN_INICIAL_ENABLED="false"
Remove-Item Env:APP_ADMIN_INICIAL_SENHA -ErrorAction SilentlyContinue
.\mvnw.cmd spring-boot:run
```

A conexão `MONGODB_URI` deve estar configurada no terminal. As contas cadastradas ficam armazenadas no banco e podem ser utilizadas nas próximas execuções.

## Perfis e rotas

Cada usuário possui um perfil. O cadastro de contas é restrito ao administrador, e as permissões são verificadas pelo Spring Security no servidor.

| Rota | Acesso | Finalidade |
| --- | --- | --- |
| `/login` | Público | Tela de autenticação |
| `/` | Usuário autenticado | Página inicial com as opções do perfil |
| `/admin/usuarios/cadastro` | `ADMINISTRADOR` | Cadastro de usuários |
| `/operador/inicio` | `OPERADOR` | Área do Operador |
| `/professor/inicio` | `PROFESSOR` | Área do Professor |
| `POST /logout` | Usuário autenticado, com CSRF | Encerramento da sessão |

Para trocar de conta, clique em **Sair** e faça login com outro usuário. Cada área exige o perfil indicado na tabela, inclusive quando a URL é acessada diretamente.

## Executar os testes

No PowerShell:

```powershell
.\mvnw.cmd test
```

Os testes verificam cadastro, validação, BCrypt, autenticação, bloqueio por tentativas, autorização dos perfis, CSRF e logout. A suíte padrão usa mocks do repositório e não exige conexão com o Atlas.

Para executar o teste de integração com o Atlas, configure `MONGODB_URI` e execute no PowerShell:

```powershell
$env:RUN_ATLAS_TESTS="true"
.\mvnw.cmd "-Dtest=MongoAtlasIntegrationTests" test
Remove-Item Env:RUN_ATLAS_TESTS
```

Esse teste consulta usuários e verifica a gravação, recuperação, expiração e exclusão de uma sessão temporária com contexto de autenticação. As contas existentes não são alteradas.

## Gerar o executável

No PowerShell:

```powershell
.\mvnw.cmd package
java -jar target/login-seguro-0.0.1-SNAPSHOT.jar
```

Defina `MONGODB_URI` antes de executar o arquivo JAR. A aplicação também fica disponível na porta 8080.

## Estrutura do projeto

O pacote principal é `br.com.loginseguro`, e a classe `LoginSeguroApplication` inicia a aplicação.

| Diretório | Responsabilidade |
| --- | --- |
| `config` | Segurança, BCrypt, sessões, administrador inicial e configuração visual |
| `controller` | Rotas HTTP e seleção das páginas |
| `dto` | Dados e validações do cadastro |
| `model` | Documento `Usuario` e perfis `Role` |
| `repository` | Acesso aos usuários no MongoDB |
| `security` | Carregamento das contas e eventos de autenticação |
| `service` | Regras de cadastro e controle de tentativas de login |
| `src/main/resources/templates` | Páginas Thymeleaf e fragmentos reutilizáveis |
| `src/main/resources/static` | Arquivos CSS e JavaScript |
| `src/test/java` | Testes automatizados |

Os controllers recebem as requisições, os DTOs validam os formulários, os serviços aplicam as regras e os repositories acessam o banco. Os templates apresentam os dados sem executar regras de cadastro ou autenticação.

A coleção `usuarios` possui índices únicos em `nomeUsuario` e `email`. Esses campos são normalizados para minúsculas, e a senha fica no campo `senhaHash`. As sessões ficam na coleção `sessions`, administrada por `mongodb-spring-session`.

## Segurança e apresentação

- Credenciais do banco são recebidas por variável de ambiente e não devem ser versionadas.
- Senhas são protegidas com BCrypt.
- Formulários POST utilizam tokens CSRF fornecidos pela integração entre Thymeleaf e Spring Security.
- A autenticação renova a sessão, e o logout a invalida.
- Cookies de sessão usam `HttpOnly` e `SameSite=Lax`. Para execução via HTTPS, configure `SERVER_SERVLET_SESSION_COOKIE_SECURE=true`.
- Cores ficam em `static/css/theme.css`; layouts ficam em `login.css` e `app.css`; os fragmentos `head` e `topbar` são compartilhados pelas páginas.

O nome exibido e o arquivo de tema podem ser configurados antes da execução:

```powershell
$env:APP_INTERFACE_NOME="Minha aplicação"
$env:APP_INTERFACE_TEMA_CSS="/css/tema-personalizado.css"
```

Para criar um tema, copie `src/main/resources/static/css/theme.css` para `tema-personalizado.css` no mesmo diretório e altere os valores das variáveis CSS. `InterfaceConfig` fornece o nome e o caminho do tema aos templates.
