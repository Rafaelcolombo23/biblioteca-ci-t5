# Resultados e evidências de execução

Validamos o pipeline em duas situações: uma execução que detectou um defeito no cadastro após a carga inicial e outra que aprovou a aplicação após a correção. Preservamos os resultados, os commits e os relatórios para relacionar cada comportamento à versão testada.

Repositório: [Rafaelcolombo23/biblioteca-ci-t5](https://github.com/Rafaelcolombo23/biblioteca-ci-t5).

Pipeline: [Biblioteca CI — T5](https://github.com/Rafaelcolombo23/biblioteca-ci-t5/actions).

Configuração: [`.github/workflows/ci.yml`](../.github/workflows/ci.yml).

## 1. Execução bem-sucedida

| Campo | Registro |
| --- | --- |
| Estado | Sucesso: validação, build, testes, segurança, identificação do pacote, publicação, smoke e upload das evidências concluídos. |
| URL da execução | [Execução 38650549205](https://github.com/Rafaelcolombo23/biblioteca-ci-t5/actions/runs/38650549205). |
| Evento e referência | `push` em `main`. |
| SHA efetivamente testado | [`7294b5e109278c74c9b65a91a63d54b5c29102a4`](https://github.com/Rafaelcolombo23/biblioteca-ci-t5/commit/7294b5e109278c74c9b65a91a63d54b5c29102a4). |
| Identificador e tentativa | Execução `38650549205`, tentativa `1`. |
| Data e duração observadas | 14/10/2026, 22:52:29–22:53:47 UTC: 78 segundos entre criação e conclusão. Job: 22:52:32–22:53:46 UTC, 74 segundos. |
| Resultado dos testes | `Tests run: 8, Failures: 0, Errors: 0, Skipped: 0`; [relatório Surefire](evidencias/sucesso/relatorio-testes.txt). |
| Resultado do scanner | Gitleaks aprovado, zero achados no intervalo analisado; [`gitleaks.sarif`](evidencias/sucesso/gitleaks.sarif) contém `results: []`. |
| Resultado do smoke HTTP | `GET /livros -> HTTP 200`; conteúdo esperado aprovado, conforme [`smoke.log`](evidencias/sucesso/smoke.log). |
| Artefato do JAR | Pacote `biblioteca-7294b5e109278c74c9b65a91a63d54b5c29102a4`, contendo `biblioteca-7294b5e109278c74c9b65a91a63d54b5c29102a4.jar`, `MANIFEST.txt` e `SHA256SUMS`. |
| Artefato de evidências | `evidencias-7294b5e109278c74c9b65a91a63d54b5c29102a4-38650549205-1`; estados de todas as etapas em [`execution.txt`](evidencias/sucesso/execution.txt). |
| Manifesto e checksum | [`MANIFEST.txt`](evidencias/sucesso/MANIFEST.txt) e [`SHA256SUMS`](evidencias/sucesso/SHA256SUMS), preservados do pacote da execução. |
| SHA-256 registrado do JAR | `aa5ee28295c733bb03fa601a72f63b1ab899041e6b7dc554082d1c80939ae89b`. |

A execução concluiu build, testes, segurança, publicação e smoke com sucesso. O manifesto registra Java Temurin 21.0.10+7, Maven 3.9.15, commit, evento e URL da execução. Confirmamos também o funcionamento do JAR pelo teste HTTP realizado após a publicação.

## 2. Falha real detectada: cadastro após carga inicial

| Campo | Registro |
| --- | --- |
| Estado | Falha comprovada no GitHub Actions e evidências baixadas. |
| Defeito | IDs explícitos de `data.sql` colidem com IDs gerados pelo H2 no primeiro cadastro após a carga inicial. |
| Teste que detecta | `cadastraAutorELivroAposCargaInicialEPersisteRelacionamento`. |
| Resultado local anterior à correção | Oito testes executados: sete passaram e um apresentou erro de chave duplicada ao inserir autor com ID 1. |
| URL da execução com falha | [Execução 38650134855](https://github.com/Rafaelcolombo23/biblioteca-ci-t5/actions/runs/38650134855). |
| Evento, referência e tentativa | `push` em `main`, tentativa `1`. |
| SHA efetivamente testado | [`595a9e4c288ee9d9054b2f270ef1d7a75ebe6399`](https://github.com/Rafaelcolombo23/biblioteca-ci-t5/commit/595a9e4c288ee9d9054b2f270ef1d7a75ebe6399). |
| Data e duração observadas | 14/10/2026, 22:48:38–22:49:18 UTC: 40 segundos entre criação e conclusão. Job: 22:48:41–22:49:17 UTC, 36 segundos. |
| Etapa que detectou a falha | `Executar testes bloqueantes e verificar pacote`; validação e compilação anteriores concluíram com sucesso. |
| Mensagem/relatório observado | `Tests run: 8, Failures: 0, Errors: 1, Skipped: 0`; `DataIntegrityViolationException` e violação de chave primária `PUBLIC.AUTOR(ID)`, chave `1`. |
| Etapas dependentes interrompidas | Segurança, identificação do pacote, publicação do JAR e smoke ficaram `skipped`. A coleta e o upload de evidências concluíram com sucesso. |
| Evidências preservadas | Artefato `evidencias-595a9e4c288ee9d9054b2f270ef1d7a75ebe6399-38650134855-1`; cópias de [`execution.txt`](evidencias/falha/execution.txt) e [`relatorio-testes.txt`](evidencias/falha/relatorio-testes.txt) versionadas neste repositório. |
| Commit da correção | [`7294b5e109278c74c9b65a91a63d54b5c29102a4`](https://github.com/Rafaelcolombo23/biblioteca-ci-t5/commit/7294b5e109278c74c9b65a91a63d54b5c29102a4). |
| Correção aplicada | `data.sql` deixou de fornecer IDs explícitos nos inserts; H2 gera os IDs dos autores/livros, e os livros obtêm `autor_id` por subconsulta ao nome do autor. O teste de regressão permanece no projeto. |
| Execução posterior à correção | [38650549205](https://github.com/Rafaelcolombo23/biblioteca-ci-t5/actions/runs/38650549205), com oito testes e todas as etapas aprovados, conforme seção 1. |

O teste de regressão revelou a colisão de IDs e interrompeu o fluxo antes da publicação do JAR. Corrigimos a carga inicial para utilizar os identificadores gerados pelo H2 e validamos a alteração localmente e no CI, mantendo o mesmo teste. Os relatórios preservados registram o diagnóstico e o resultado da correção.

### Reprodução da falha e da correção

A reprodução utiliza JDK 21 e um checkout sem alterações locais, verificável com `git status --short`. O commit anterior à correção é selecionado em modo destacado, preservando a referência da branch `main`:

```sh
git switch --detach 595a9e4c288ee9d9054b2f270ef1d7a75ebe6399
./mvnw --batch-mode --no-transfer-progress -DfailIfNoTests=true clean verify
```

Nesse commit, o resultado é `BUILD FAILURE`, com erro de chave duplicada no teste de cadastro após carga inicial. A execução da versão corrigida utiliza:

```sh
git switch main
./mvnw --batch-mode --no-transfer-progress -DfailIfNoTests=true clean verify
```

Na versão corrigida, o resultado é `BUILD SUCCESS`, com oito testes aprovados. O comando equivalente no Windows/PowerShell utiliza `.\mvnw.cmd` no lugar de `./mvnw`.

## 3. Verificação local

| Campo | Registro |
| --- | --- |
| Estado | Build, testes e smoke locais aprovados após corrigir a carga inicial. |
| Sistema operacional e JDK | macOS arm64, Microsoft OpenJDK 21.0.10. |
| Commit ou estado validado | Código da correção `7294b5e109278c74c9b65a91a63d54b5c29102a4`. |
| Comando executado | `./mvnw --batch-mode --no-transfer-progress -DfailIfNoTests=true clean verify`. |
| Resultado e código de saída | `BUILD SUCCESS`, código `0`. |
| Contagem de testes | Oito executados, zero falhas, zero erros e zero ignorados. |
| Smoke local | O próprio JAR iniciou e respondeu HTTP `200` em `/livros`, com o conteúdo esperado. Processo encerrado pelo script. Este resultado local é separado da execução no GitHub Actions. |

## 4. Preservação dos registros

Preservamos os relatórios de testes, os estados das etapas, o resultado HTTP, o manifesto, o checksum e o relatório SARIF em `docs/evidencias/`. Esses arquivos mantêm o histórico técnico das execuções além dos 30 dias de retenção configurados para os artefatos do GitHub Actions.

O repositório público reúne o código, a documentação e os relatórios selecionados. O JAR e os logs completos foram publicados nos artefatos das respectivas execuções. Os links e os SHAs registrados estabelecem a relação entre o defeito identificado, a alteração em `data.sql` e a versão aprovada.
