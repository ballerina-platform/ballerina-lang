#!bin/bash

WORK_DIR=`pwd`

MYSQL_HOST=$1
MYSQL_PORT=$2
MYSQL_USERNAME=$3
MYSQL_PASSWORD=$4

mysql -h${MYSQL_HOST} -P${MYSQL_PORT} -u${MYSQL_USERNAME} -p${MYSQL_PASSWORD} <product-scenarios/mysql_init.sql