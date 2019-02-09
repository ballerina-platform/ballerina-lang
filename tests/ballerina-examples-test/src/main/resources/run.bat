@echo off

REM ---------------------------------------------------------------------------
REM   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
REM
REM   Licensed under the Apache License, Version 2.0 (the "License");
REM   you may not use this file except in compliance with the License.
REM   You may obtain a copy of the License at
REM
REM   http://www.apache.org/licenses/LICENSE-2.0
REM
REM   Unless required by applicable law or agreed to in writing, software
REM   distributed under the License is distributed on an "AS IS" BASIS,
REM   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
REM   See the License for the specific language governing permissions and
REM   limitations under the License.
REM ---------------------------------------------------------------------------
REM Run Ballerina examples.
REM ---------------------------------------------------------------------------

REM cd %1
REM..\bin\ballerina.bat init
REM set include=hello-world-service hello-world hello-world-parallel hello-world-client hello-world-streams modules variables var functions functions-with-defaultable-parameters functions-with-rest-parameter documentation if-else while foreach match match-expression type-guard elvis-operator function-pointers anonymous-functions closures functional-iteration functions-as-entry-points records record-optional-fields values string-template symbolic-string-literal arrays records maps table table-queries union-type optional-type nil-lifting tuple-type any-type any-data-type type-conversion length integer-ranges equality immutable-values json json-arrays constrained-json xml xml-literal xml-namespaces xml-attributes xml-functions json-record-map-conversion xml-access byte-io character-io record-io csv-io json-io xml-io workers worker-interaction fork fork-variable-access async locks channels-correlation channels-workers objects object-initializer object-member-functions object-assignability abstract-objects object-type-reference error-handling error-lifting check panic trap-error task-timer task-appointment streams join-multiple-streams temporal-aggregations-and-windows identify-patterns identify-trends cache math-functions strings xml-to-json-conversion h2-client mysql-client jdbc-client-crud-operations jdbc-client-batch-update jdbc-client-call-procedures streaming-big-dataset local-transactions xa-transactions transactions-distributed secured-service-with-jwt secured-client-with-basic-auth secured-client-with-jwt-auth secured-client-with-oauth2 testerina-assertions testerina-before-and-after-test testerina-before-each-test testerina-before-and-after-suite testerina-data-driven-tests testerina-guarantee-test-execution-order testerina-group-tests docker-deployment kubernetes-deployment http-client-endpoint http-redirects base-path-and-path query-path-matrix-param restrict-by-media-type http-caching-client http-disable-chunking http-trace-logs basic-https-listener-client https-listener mutual-ssl request-with-multiparts response-with-multiparts http-cors http-access-logs http-data-binding http-100-continue different-payload-types http-streaming http-compression http-1.1-to-2.0-protocol-switch http-2.0-server-push websocket-basic-sample http-to-websocket-upgrade websocket-proxy-server websocket-chat-application header-based-routing passthrough content-based-routing http-circuit-breaker http-load-balancer http-failover http-retry http-timeout websub-internal-hub-sample websub-service-integration-sample directory-listener grpc-unary-blocking grpc-unary-non-blocking grpc-server-streaming grpc-client-streaming grpc-bidirectional-streaming grpc-secured-unary tracing client-generation ballerina-to-swagger jms-simple-queue-message-receiver jms-simple-queue-message-producer jms-simple-topic-message-subscriber jms-simple-topic-message-producer jms-durable-topic-message-subscriber jms-queue-message-receiver jms-queue-message-producer jms-topic-message-subscriber jms-topic-message-producer jms-synchronous-queue-message-receiver jms-simple-durable-topic-message-subscriber transactional-queue-message-producer jms-headers-and-properties send-jms-message-to-http-endpoint counter-metrics gauge-metrics
REM for %%a in (%include%) do (
REM  ..\bin\ballerina.bat build %%a
REM )