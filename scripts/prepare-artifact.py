#!/usr/bin/env python3
"""Identifica o JAR já verificado; não executa build nem pula testes."""

import hashlib
import os
from pathlib import Path
import re
import shutil
import subprocess


def output(*command):
    return subprocess.check_output(command, text=True, stderr=subprocess.STDOUT).strip()


commit = output("git", "rev-parse", "HEAD")
if os.environ.get("GITHUB_SHA", commit) != commit:
    raise SystemExit("O checkout não corresponde ao commit do evento.")

jars = list(Path("target").glob("*.jar"))
if len(jars) != 1:
    raise SystemExit(f"Esperado um único JAR executável em target; encontrados {len(jars)}.")

destination = Path("dist")
destination.mkdir(exist_ok=True)
# Remove somente saídas conhecidas desta ferramenta, evitando misturar commits.
previous_outputs = list(destination.iterdir())
for previous in previous_outputs:
    if not previous.is_file() or not re.fullmatch(r"biblioteca-[0-9a-f]{40}\.jar|MANIFEST\.txt|SHA256SUMS", previous.name):
        raise SystemExit(f"Arquivo inesperado em dist: {previous.name}; revise antes de continuar.")
for previous in previous_outputs:
    previous.unlink()
artifact = destination / f"biblioteca-{commit}.jar"
shutil.copy2(jars[0], artifact)
checksum = hashlib.sha256(artifact.read_bytes()).hexdigest()
(destination / "SHA256SUMS").write_text(f"{checksum}  {artifact.name}\n", encoding="utf-8")

run_url = "execução local"
if os.environ.get("GITHUB_RUN_ID"):
    run_url = (
        f"{os.environ['GITHUB_SERVER_URL']}/{os.environ['GITHUB_REPOSITORY']}"
        f"/actions/runs/{os.environ['GITHUB_RUN_ID']}"
    )

manifest = [
    f"artifact={artifact.name}",
    f"sha256={checksum}",
    f"commit={commit}",
    f"pr_head={os.environ.get('PR_HEAD_SHA', '')}",
    f"event={os.environ.get('GITHUB_EVENT_NAME', 'local')}",
    f"run={run_url}",
    f"attempt={os.environ.get('GITHUB_RUN_ATTEMPT', '')}",
    "promotion=Requer CI completo aprovado, smoke aprovado e revisão humana.",
    "",
    "Java:",
    output("java", "-version"),
    "",
    "Maven:",
    output("./mvnw", "--version"),
]
(destination / "MANIFEST.txt").write_text("\n".join(manifest) + "\n", encoding="utf-8")
print(f"Artefato: {artifact}; SHA-256: {checksum}")
