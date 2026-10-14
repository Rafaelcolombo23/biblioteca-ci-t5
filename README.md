# Biblioteca — Integração Contínua

[![Pipeline CI](https://github.com/Rafaelcolombo23/biblioteca-ci-t5/actions/workflows/ci.yml/badge.svg)](https://github.com/Rafaelcolombo23/biblioteca-ci-t5/actions/workflows/ci.yml)

Desenvolvemos uma aplicação de biblioteca para gerenciar autores e livros e automatizamos seu processo de integração contínua com GitHub Actions. Nosso objetivo foi reunir compilação, testes, segurança e publicação em um fluxo reproduzível, com identificação de cada versão e registros dos resultados.

| Integrantes | RA |
| --- | --- |
| Rafael Colombo da Silva | 14551 |
| Ryan Alexandre da Costa | 14647 |

- [Relatório do projeto](docs/TRABALHO-5.md)
- [Resultados e evidências de execução](docs/EVIDENCIAS.md)
- [Definição do pipeline](.github/workflows/ci.yml)
- [Execuções no GitHub Actions](https://github.com/Rafaelcolombo23/biblioteca-ci-t5/actions)

## Tecnologias

Utilizamos Java 21, Spring Boot 4.0.6, Spring Data JPA, Thymeleaf e H2 em memória. O Maven Wrapper fixa a versão 3.9.15 do Maven, e o ambiente de CI utiliza Ubuntu 24.04 com Java Temurin 21.0.10.

A aplicação permite cadastrar, consultar, editar e excluir autores e livros, buscar livros por título e navegar pelo catálogo com paginação. Os dados iniciais incluem três autores e cinco livros. O banco é recriado a cada inicialização; a configuração adotada atende ao ambiente de desenvolvimento e aos testes.

## Execução local

A execução requer Git, JDK 21 com `JAVA_HOME` configurado e acesso à internet para o primeiro download das dependências. O Maven é obtido pelo próprio Wrapper.

```sh
git clone https://github.com/Rafaelcolombo23/biblioteca-ci-t5.git
cd biblioteca-ci-t5
```

Linux/macOS:

```sh
./mvnw spring-boot:run
```

Windows/PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

A aplicação fica disponível em [localhost:8080](http://localhost:8080), com as páginas `/autores` e `/livros`. O console local do H2 fica em `/h2-console`, com URL `jdbc:h2:mem:biblioteca`, usuário `sa` e senha vazia. O encerramento do processo é feito com `Ctrl+C`.

## Build e testes

Linux/macOS:

```sh
./mvnw --batch-mode --no-transfer-progress -DfailIfNoTests=true clean verify
java -jar target/biblioteca-0.0.1-SNAPSHOT.jar
```

Windows/PowerShell:

```powershell
.\mvnw.cmd --batch-mode --no-transfer-progress -DfailIfNoTests=true clean verify
java -jar target/biblioteca-0.0.1-SNAPSHOT.jar
```

O comando remove a saída anterior, compila a aplicação, executa os testes e gera o JAR. A opção `-DfailIfNoTests=true` impede aprovação sem testes encontrados. Os relatórios ficam em `target/surefire-reports/`.

Implementamos oito testes de integração para carga inicial, relacionamento entre autores e livros, busca por título, paginação, cadastro após a carga inicial, ISBN único, integridade referencial e consulta de entidades inexistentes. Durante a validação, identificamos e corrigimos uma colisão de IDs no H2. As execuções anteriores e posteriores à correção estão registradas em [Resultados e evidências](docs/EVIDENCIAS.md).

O teste HTTP do JAR é executado com Bash e `curl`:

```sh
bash scripts/smoke-test.sh target/biblioteca-0.0.1-SNAPSHOT.jar
```

O script inicia a aplicação, verifica a página de livros e encerra o processo, registrando os resultados em `ci-evidence/`. A porta padrão é 8080 e pode ser alterada com `SMOKE_PORT=8081`. No Windows, o script utiliza Git Bash ou WSL.

## Pipeline

Definimos três gatilhos: `push` na `main`, `pull_request` com destino à `main` e acionamento manual por `workflow_dispatch`. O fluxo completo também pode ser iniciado em **Actions → Biblioteca CI — T5 → Run workflow**.

```mermaid
flowchart LR
  A[Evento] --> B[Checkout]
  B --> C[Validação inicial]
  C --> D[Build]
  D --> E[Testes]
  E --> F[Qualidade e segurança]
  F --> G[JAR + manifesto + SHA-256]
  G --> H[Publicação em Actions]
  H --> I[Deploy temporário + smoke HTTP]
  I --> J[Revisão humana para promoção]
```

O build parte de uma saída limpa e utiliza cache apenas para dependências. Uma falha interrompe as etapas seguintes, enquanto a coleta dos registros permanece habilitada. Em pull requests, o checkout usa o commit de integração gerado pelo GitHub, cujo SHA é registrado nos metadados da execução.

O teste HTTP inicia o próprio JAR com H2 em memória e console desabilitado, aguarda até 90 segundos e verifica HTTP 200 e os textos `Livros` e `Novo livro` em `/livros`. Definimos a conclusão de todas as etapas e a revisão humana das evidências como condições para promoção. O ambiente implantado pelo pipeline é temporário e não inclui produção.

## Artefatos e rastreabilidade

O pacote `biblioteca-<SHA completo do commit>` contém o JAR, `MANIFEST.txt` e `SHA256SUMS`. O pacote `evidencias-<SHA>-<run_id>-<run_attempt>` reúne logs, relatórios dos testes, resultado do scanner e teste HTTP.

Os artefatos do Actions têm retenção de 30 dias. Preservamos os relatórios selecionados em `docs/evidencias/`, mantendo o registro da falha e da execução bem-sucedida no histórico do repositório. A investigação de falhas utiliza o primeiro passo com erro, os relatórios Surefire e a associação entre execução e commit. Essa rastreabilidade permitiu localizar a causa da colisão de IDs e verificar sua correção com o mesmo teste de regressão.

## Segurança

Adotamos `contents: read` para o `GITHUB_TOKEN`, ações fixadas por commit e detecção de segredos com Gitleaks e valores sensíveis ocultados nos relatórios. O build e os testes utilizam H2 em memória e não dependem de credenciais externas. Arquivos locais de configuração sensível estão excluídos pelo `.gitignore`.

A verificação de segredos complementa os testes e a revisão do código. A implementação atual não inclui análise de vulnerabilidades das dependências nem medição de cobertura; essas verificações permanecem como possibilidades de evolução do projeto.

## Estrutura do projeto

```text
.github/workflows/ci.yml  Pipeline de integração contínua
.mvn/wrapper/            Configuração do Maven Wrapper
src/main/                Aplicação Biblioteca
src/test/                Testes de integração
scripts/                 Identificação do pacote e teste HTTP
docs/TRABALHO-5.md       Relatório técnico do projeto
docs/EVIDENCIAS.md       Resultados das execuções
docs/evidencias/         Relatórios preservados
```
