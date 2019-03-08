#!/bin/bash

parent_path=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )
grand_parent_path=$(dirname ${parent_path})
great_grand_parent_path=$(dirname ${grand_parent_path})

. ${great_grand_parent_path}/usage.sh
. ${great_grand_parent_path}/utils.sh

source ${great_grand_parent_path}/setup_env.sh ${INPUT_DIR} ${OUTPUT_DIR}

work_dir=$(pwd)

git clone https://github.com/ballerina-guides/data-backed-service --branch testgrid-onboarding

bal_path=data-backed-service/guide/data_backed_service/employee_db_service.bal

download_and_extract_mysql_connector

docker build -t ballerinascenarios/mysql-ballerina:1.0 data-backed-service/resources/

docker login --username=ballerinascenarios --password=ballerina75389

docker push ballerinascenarios/mysql-ballerina:1.0

sed -i "s/mysql-ballerina/ballerinascenarios\/mysql-ballerina/" data-backed-service/resources/kubernetes/mysql-deployment.yaml

kubectl create -f data-backed-service/resources/kubernetes/

sed -i "s/default = \"localhost\"/default = \"mysql-service\"/" ${bal_path}

sed -i "s/<BALLERINA_VERSION>/${infra_config["BallerinaVersion"]}/" ${bal_path}

sed -i "s:<path_to_JDBC_jar>:"${parent_path}/mysql-connector-java-5.1.47/mysql-connector-java-5.1.47.jar":g" ${bal_path}

sed -i "s:<USERNAME>:ballerinascenarios:g" ${bal_path}

sed -i "s:<PASSWORD>:ballerina75389:g" ${bal_path}

sed -i "s:ballerina.guides.io:ballerinascenarios:g" ${bal_path}

cat ${bal_path}

cd data-backed-service/guide

${ballerina_home}/bin/ballerina build data_backed_service --skiptests

cd ../..

kubectl apply -f ${work_dir}/data-backed-service/guide/target/kubernetes/data_backed_service

wait_for_pod_readiness

kubectl get pods

kubectl get svc ballerina-guides-employee-database-service -o=json

kubectl get nodes --output wide

kubectl get ingress

kubectl get nodes -o=json

EXTERNAL_IP=$(kubectl get svc ballerina-guides-employee-database-service -o jsonpath='{.status.loadBalancer.ingress[0].hostname}'

echo "ExternalIP=$EXTERNAL_IP" >> ${OUTPUT_DIR}/deployment.properties
