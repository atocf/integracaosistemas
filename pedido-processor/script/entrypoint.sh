#!/bin/bash

# Criar diretórios necessários
mkdir -p /app/upload_files/processing
mkdir -p /app/upload_files/completed

# Iniciar a aplicação
exec "$@"