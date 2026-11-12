@echo off
if not exist dist\BancoDados.jar call compilar.bat
java -jar dist\BancoDados.jar %*
