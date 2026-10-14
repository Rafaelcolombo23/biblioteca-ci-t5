#!/usr/bin/env bash
# Executa o JAR real em um ambiente efêmero e encerra somente o processo criado aqui.
set -euo pipefail

jar_path="${1:-target/biblioteca-0.0.1-SNAPSHOT.jar}"
evidence_dir="${2:-ci-evidence}"
smoke_port="${SMOKE_PORT:-8080}"
test -s "$jar_path"
mkdir -p "$evidence_dir"

java -jar "$jar_path" \
  --server.address=127.0.0.1 \
  --server.port="$smoke_port" \
  '--spring.datasource.url=jdbc:h2:mem:biblioteca_smoke;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE' \
  --spring.jpa.hibernate.ddl-auto=create-drop \
  --spring.h2.console.enabled=false \
  > "$evidence_dir/application.log" 2>&1 &
application_pid=$!

cleanup() {
  kill "$application_pid" 2>/dev/null || true
  wait "$application_pid" 2>/dev/null || true
}
trap cleanup EXIT
trap 'exit 130' INT
trap 'exit 143' TERM

ready=false
for attempt in $(seq 1 45); do
  if ! kill -0 "$application_pid" 2>/dev/null; then
    printf 'A aplicação encerrou durante a inicialização. Consulte application.log.\n' >&2
    exit 1
  fi
  # Evita aceitar a resposta de outro processo que já estivesse na porta.
  if grep -Fq 'Started BibliotecaApplication' "$evidence_dir/application.log"; then
    ready=true
    break
  fi
  sleep 2
done

if [ "$ready" != true ]; then
  printf 'Timeout: aplicação não iniciou em até 90 segundos.\n' >&2
  exit 1
fi

http_status=$(curl --silent --show-error --connect-timeout 2 --max-time 10 \
  --output "$evidence_dir/livros.html" \
  --dump-header "$evidence_dir/http-headers.txt" \
  --write-out '%{http_code}' \
  "http://127.0.0.1:${smoke_port}/livros")
printf 'GET /livros -> HTTP %s\n' "$http_status"
test "$http_status" = 200
grep -Fq '<h1>Livros</h1>' "$evidence_dir/livros.html"
grep -Fq 'Novo livro' "$evidence_dir/livros.html"
printf 'Smoke aprovado: HTTP 200 e conteúdo esperado da página de livros.\n'
