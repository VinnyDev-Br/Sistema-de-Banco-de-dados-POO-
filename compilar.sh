#!/bin/sh
# Compila o projeto e gera dist/BancoDados.jar (precisa do JDK 11 ou superior)
set -e
rm -rf out && mkdir -p out dist
javac --release 11 -encoding UTF-8 -d out $(find src -name '*.java')
mkdir -p out/recursos && cp -r src/recursos/. out/recursos/ 2>/dev/null || true
printf 'Main-Class: app.Main\n' > out/manifest.txt
jar cfm dist/BancoDados.jar out/manifest.txt -C out .
echo "Pronto: dist/BancoDados.jar"
