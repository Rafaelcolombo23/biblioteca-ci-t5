# Biblioteca — Trabalho 5

[![Pipeline CI](https://github.com/Rafaelcolombo23/biblioteca-ci-t5/actions/workflows/ci.yml/badge.svg)](https://github.com/Rafaelcolombo23/biblioteca-ci-t5/actions/workflows/ci.yml)

Aplicação de biblioteca com cadastro de autores e livros, preparada para o trabalho **Pipeline CI mínimo e reprodutível**. O projeto usa Java 21, Spring Boot 4.0.6, Maven 3.9.15 pelo Maven Wrapper, Spring Data JPA, Thymeleaf e H2 em memória.

| Integrantes informados | RA |
| --- | --- |
| Rafael Colombo da Silva | 14551 |
| Ryan Alexandre da Costa | 14647 |

- [Documentação do trabalho, na ordem dos slides 57–67](docs/TRABALHO-5.md)
- [Evidências de sucesso e de falha detectada](docs/EVIDENCIAS.md)
- [Definição do pipeline](.github/workflows/ci.yml)
- [Execuções no GitHub Actions](https://github.com/Rafaelcolombo23/biblioteca-ci-t5/actions)

## Executar a aplicação

Pré-requisitos: Git, JDK 21 e acesso à internet na primeira execução para baixar o Maven e as dependências. Não é necessário instalar o Maven separadamente. Confira `java -version` e configure `JAVA_HOME` para o JDK 21.

```sh
git clone https://github.com/Rafaelcolombo23/biblioteca-ci-t5.git
cd biblioteca-ci-t5
```

Linux/macOS:

```sh
./mvnw spring-boot:run
```

Windows, no PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

Acesse [http://localhost:8080](http://localhost:8080). As telas de autores e livros estão em `/autores` e `/livros`. Encerre com `Ctrl+C`.

O H2 é um banco **efêmero**: os dados são recriados ao iniciar e não persistem após encerrar a aplicação. O console H2 em `/h2-console` é destinado ao estudo local. A configuração usa usuário `sa`, senha vazia e URL `jdbc:h2:mem:biblioteca`; não se trata de uma credencial de serviço externo. O projeto não está configurado para operação em produção.

## Reproduzir build e testes

Linux/macOS:

```sh
./mvnw --batch-mode --no-transfer-progress -DfailIfNoTests=true clean verify
java -jar target/biblioteca-0.0.1-SNAPSHOT.jar
```

Windows, no PowerShell:

```powershell
.\mvnw.cmd --batch-mode --no-transfer-progress -DfailIfNoTests=true clean verify
java -jar target/biblioteca-0.0.1-SNAPSHOT.jar
```

O comando `clean verify` remove os resultados anteriores, compila, executa os testes e empacota o JAR. A opção `-DfailIfNoTests=true` também impede aprovação sem testes encontrados. Qualquer erro retorna código diferente de zero. Os relatórios ficam em `target/surefire-reports/`. O JAR local é `target/biblioteca-0.0.1-SNAPSHOT.jar`.

Os oito testes de integração cobrem carga inicial e relações entre autores/livros, busca por título, paginação, cadastro após a carga inicial, ISBN único, proteção contra exclusão de autor com livros e entidades inexistentes. A evidência de falha usa um defeito real da carga inicial: IDs explícitos podiam colidir com novos IDs gerados pelo H2. A execução antes e depois da correção deve ser consultada em [EVIDENCIAS.md](docs/EVIDENCIAS.md).

Para executar o smoke HTTP local no Linux/macOS, com Bash e `curl` disponíveis:

```sh
bash scripts/smoke-test.sh target/biblioteca-0.0.1-SNAPSHOT.jar
```

O script inicia e encerra o processo, usa a porta 8080 e grava resultados em `ci-evidence/`. Se a porta estiver ocupada, execute `SMOKE_PORT=8081 bash scripts/smoke-test.sh target/biblioteca-0.0.1-SNAPSHOT.jar`. No Windows, use Git Bash/WSL para o script ou faça a verificação manual após iniciar o JAR no PowerShell.

Esses comandos reproduzem o build e os testes Java. Para reproduzir também as verificações e os artefatos específicos da integração contínua, use **Actions → Biblioteca CI — T5 → Run workflow** no GitHub e selecione a referência desejada. O arquivo do workflow é a fonte de verdade para comandos, versões e caminhos.

## Como o pipeline funciona

O GitHub Actions inicia em `push` na `main`, em `pull_request` para `main` e por execução manual (`workflow_dispatch`). O job usa Ubuntu 24.04, Java Temurin 21.0.10 e o Maven Wrapper 3.9.15. O cache de dependências acelera downloads; o build executa `clean` para reconstruir a saída.

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

O teste HTTP inicia o próprio JAR no executor com H2 em memória e console desabilitado, aguarda até 90 segundos, verifica HTTP 200 e os textos `Livros` e `Novo livro` em `/livros`, e encerra o processo. Não há deploy de produção. A aprovação exige o job inteiro concluído com sucesso: um arquivo já publicado não significa que o teste HTTP passou.

O artefato `biblioteca-<SHA completo do commit>` contém `biblioteca-<SHA>.jar`, `MANIFEST.txt` e `SHA256SUMS`. Logs, relatórios dos testes, verificação de segredos e resultado do smoke ficam no artefato `evidencias-<SHA>-<run_id>-<run_attempt>`. A retenção configurada é de 30 dias; baixe as evidências para a entrega antes de expirarem.

Em um pull request, o checkout padrão testa o commit de integração gerado pelo GitHub. Seu SHA pode ser diferente do último commit da branch. Relacione o artefato ao SHA efetivamente testado, registrado na execução, e consulte o pull request para identificar a branch de origem.

## Investigar uma falha

1. Abra [Actions](https://github.com/Rafaelcolombo23/biblioteca-ci-t5/actions), selecione a execução e confira evento, referência e commit.
2. Abra o primeiro passo com erro; passos seguintes normalmente ficam ignorados para impedir a continuidade do fluxo.
3. Baixe o artefato `evidencias-…` e leia os logs e relatórios disponíveis. Falhas anteriores à geração de um relatório podem deixar esse arquivo ausente.
4. Para falha de teste, leia `target/surefire-reports/` e execute novamente `./mvnw --batch-mode --no-transfer-progress -DfailIfNoTests=true clean verify` no mesmo commit; no Windows, use `.\mvnw.cmd`.
5. Corrija a causa e envie um novo commit. Consulte [EVIDENCIAS.md](docs/EVIDENCIAS.md) para o registro do defeito detectado e da correção.

Uma falha de autenticação, download ou publicação também deve ser investigada pelo log da etapa correspondente. A coleta final de evidências é configurada para ocorrer mesmo após uma falha anterior; indisponibilidade do próprio GitHub ou interrupção do executor pode impedir o upload.

## Segredos e segurança básica

O build, os testes e o H2 em memória não precisam de segredos externos. O workflow solicita somente `contents: read` para o `GITHUB_TOKEN`, usa ações referenciadas por commit e executa detecção de segredos com saída redigida. Não há credencial de produção nem chave de deploy no projeto.

Não coloque tokens, senhas reais, chaves privadas ou arquivos `.env` no repositório nem em comandos que os imprimam. Se uma integração futura precisar de credenciais, use **Settings → Secrets and variables → Actions**, com permissões mínimas. Um segredo publicado deve ser revogado e substituído; apagar a linha não remove a exposição do histórico.

A verificação de segredos é uma camada básica e não substitui revisão de código, análise de vulnerabilidades das dependências ou testes de segurança. Consulte os riscos e as limitações na documentação do trabalho.

## Organização e entrega

```text
.github/workflows/ci.yml  Definição executável do CI
.mvn/wrapper/            Versão do Maven usada pelo Wrapper
src/main/                Aplicação Biblioteca
src/test/                Testes automatizados
scripts/                 Identificação do pacote e smoke HTTP
docs/TRABALHO-5.md       Atividade e documentação dos slides 57–67
docs/EVIDENCIAS.md       Registro verificável de sucesso e falha
```

Os dois integrantes informados estão identificados acima; a organização da equipe deve seguir a orientação do professor. Identificação da turma e data exata da Aula 10 ainda devem ser preenchidas. A disponibilidade real das execuções e das evidências está registrada em [EVIDENCIAS.md](docs/EVIDENCIAS.md); a descrição de uma etapa não comprova que ela já foi executada.
