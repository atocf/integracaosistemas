#!/bin/bash

# Inicia o RabbitMQ em segundo plano
rabbitmq-server &

# Aguarda o RabbitMQ iniciar completamente
sleep 10

# Executa o script de inicialização do RabbitMQ
/scripts/rabbitmq-init.sh

# Mantém o contêiner ativo
wait