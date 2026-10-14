# Evidências de execução — Trabalho 5

Este arquivo registra resultados observados e links verificáveis. **Falha real detectada pelo CI, corrigida e seguida de execução completa com sucesso no GitHub Actions.** As duas execuções e seus relatórios estão preservados abaixo.

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

Build, testes, segurança, publicação e smoke foram conferidos e estão aprovados. O manifesto identifica Java Temurin 21.0.10+7, Maven 3.9.15, commit, evento e URL da execução. A publicação de um artefato, isoladamente, não comprova o sucesso do job inteiro; nesta execução, o smoke posterior à publicação também passou.

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

O CI executou o teste de regressão com a carga inicial anterior à correção e interrompeu as etapas dependentes ao detectar a colisão de IDs. A correção foi aplicada em seguida e validada localmente e no CI com o mesmo teste. Os arquivos de texto acima preservam o resultado da falha mesmo após o vencimento do download do artefato do Actions.

### Reproduzir a falha sem alterar a `main`

Use um checkout limpo: execute `git status --short` e prossiga somente se não houver saída. Se houver alterações locais, preserve-as antes de trocar a revisão. No Linux/macOS:

```sh
git switch --detach 595a9e4c288ee9d9054b2f270ef1d7a75ebe6399
./mvnw --batch-mode --no-transfer-progress -DfailIfNoTests=true clean verify
```

O resultado esperado é `BUILD FAILURE`, com erro de chave duplicada no teste de cadastro após carga inicial. A referência destacada permite inspecionar o commit antigo sem mover a branch `main`. Depois da falha esperada, volte à versão corrigida e repita:

```sh
git switch main
./mvnw --batch-mode --no-transfer-progress -DfailIfNoTests=true clean verify
```

O resultado esperado na versão corrigida é `BUILD SUCCESS`, com oito testes aprovados. No Windows/PowerShell, substitua `./mvnw` por `.\mvnw.cmd`; os comandos Git são os mesmos.

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

## 4. Acesso e conservação das evidências

1. Abrir a URL da execução e conferir SHA, referência, evento e status.
2. Conferir o primeiro passo com erro na execução vermelha e os passos seguintes ignorados.
3. Abrir os logs ou baixar o artefato `evidencias-<SHA>-<run_id>-<run_attempt>`.
4. Na execução verde, baixar `biblioteca-<SHA>` e conferir JAR, manifesto e checksum.
5. Guardar os downloads antes de completarem os **30 dias** de retenção configurados.

Os relatórios de texto, estados das etapas, resultado HTTP, manifesto, checksum e SARIF selecionados foram copiados para `docs/evidencias/`. Essas cópias permanecem no repositório após a expiração dos artefatos do Actions. O JAR completo e os logs extensos são disponibilizados nos artefatos da execução.

O repositório público permite consultar código e páginas de execução. O download de artefatos do GitHub Actions pode exigir uma conta autenticada. Não publicar dados pessoais, credenciais ou logs sensíveis como material de entrega.

## 5. Critério para considerar a entrega comprovada

- [x] Repositório criado com visibilidade pública.
- [x] URL de uma execução completa com sucesso registrada.
- [x] SHA testado e artefato correspondente registrados.
- [x] Relatórios e resultado HTTP conferidos.
- [x] URL da execução com falha real registrada.
- [x] Relatório da falha e interrupção das etapas seguintes conferidos.
- [x] Correção na `main` comprovada com o teste de regressão mantido.
- [x] Download da execução com falha preservado; resumo e relatório versionados.
- [x] Download da execução bem-sucedida preservado para a apresentação.
- [x] Checklist do Slide 67 atualizado com resultados observados.
