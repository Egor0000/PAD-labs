#!/bin/bash

RABBITMQ_HOST="rabbitmq"
RABBITMQ_PORT="15672"
RABBITMQ_USER="guest"
RABBITMQ_PASSWORD="guest"

QUEUE_NAME_1="saga_request"
QUEUE_NAME_2="saga_response"

declare_queue() {
  rabbitmqadmin -u $RABBITMQ_USER -p $RABBITMQ_PASSWORD -H $RABBITMQ_HOST -P $RABBITMQ_PORT \
    declare queue name=$1 durable=true auto_delete=false arguments='{}'
}

# declare_queue $QUEUE_NAME_1
# declare_queue $QUEUE_NAME_2

rabbitmqadmin -u $RABBITMQ_USER -p $RABBITMQ_PASSWORD -H $RABBITMQ_HOST -P $RABBITMQ_PORT declare exchange name=saga_request type=topic
rabbitmqadmin -u $RABBITMQ_USER -p $RABBITMQ_PASSWORD -H $RABBITMQ_HOST -P $RABBITMQ_PORT declare exchange name=saga_compensate type=topic