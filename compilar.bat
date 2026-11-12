@echo off
rem Compila o projeto e gera dist\BancoDados.jar (precisa do JDK 11 ou superior)
if exist out rmdir /s /q out
mkdir out
mkdir dist 2>nul
dir /s /b src\*.java > fontes.txt
javac --release 11 -encoding UTF-8 -d out @fontes.txt || exit /b 1
del fontes.txt
echo Main-Class: app.Main> out\manifest.txt
jar cfm dist\BancoDados.jar out\manifest.txt -C out .
echo Pronto: dist\BancoDados.jar
