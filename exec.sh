#!/bin/bash

BASE_DIR=$(pwd)
LIBS_DIR="$BASE_DIR/biblis_obrigatorias"
BUILD_DIR="$BASE_DIR/compilar-manual"
PASTA_ASSETS="$BASE_DIR/gdx-android/assets"
PASTA_LIBS_ANDROID="$BASE_DIR/gdx-android/libs"

echo "--- PREPARANDO AMBIENTE ---"
mkdir -p "$LIBS_DIR" "$BUILD_DIR"

if [ -d "$PASTA_LIBS_ANDROID" ]; then
    echo "Copiando bibliotecas extras do Android..."
    cp -r "$PASTA_LIBS_ANDROID"/* "$LIBS_DIR/"
fi

# monta o Classpath
CP="$BUILD_DIR:$LIBS_DIR/*:gdx-jogo/src:gdx-pc/src"

echo "--- COMPILANDO ---"
javac -cp "$CP" $(find gdx-jogo/src -name "*.java") -d "$BUILD_DIR"
javac -cp "$CP" gdx-pc/src/com/minimine/Iniciador.java -d "$BUILD_DIR"

if [ $? -eq 0 ]; then
    echo "--- RODANDO ---"
    cd "$PASTA_ASSETS"
    
    # -Djava.library.path aponta onde estão os arquivos .so do Simplex
    java -Djava.library.path="$LIBS_DIR:$LIBS_DIR/x86_64:$LIBS_DIR/desktop" \
         -cp "$BUILD_DIR:$LIBS_DIR/*" \
         com.minimine.Iniciador
else
    echo "ERRO NA COMPILAÇÃO."
    exit 1
fi
