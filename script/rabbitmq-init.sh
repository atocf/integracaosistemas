#!/bin/bash

# Espera o RabbitMQ iniciar completamente
until rabbitmqctl status >/dev/null 2>&1; do
  echo "Waiting for RabbitMQ to start..."
  sleep 1
done

# Declara a exchange
rabbitmqadmin declare exchange name=pedido-exchange type=direct

# Declara a fila
rabbitmqadmin declare queue name=pedido-queue

# Cria o binding entre a exchange e a fila
rabbitmqadmin declare binding source=pedido-exchange destination=pedido-queue routing_key=pedido-routing-key

echo "RabbitMQ configurado com sucesso!"