#!/bin/sh
# Executa o sistema (os dados ficam na pasta ./dados)
[ -f dist/BancoDados.jar ] || ./compilar.sh
java -jar dist/BancoDados.jar "$@"
