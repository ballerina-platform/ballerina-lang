/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.syntaxapicallsgen.test.e2e;

import org.testng.annotations.Test;

/**
 * Test bbe examples to segment conversion.
 *
 * @since 2.0.0
 */
public class BbeExampleTest extends AbstractSegmentTest {
    @Test
    public void testAccessMutateJavaFields() {
        test("bbe-examples/access_mutate_java_fields.bal");
    }

    @Test
    public void testAnonymousFunctions() {
        test("bbe-examples/anonymous_functions.bal");
    }

    @Test
    public void testAnonymousRecords() {
        test("bbe-examples/anonymous_records.bal");
    }

    @Test
    public void testAnyType() {
        test("bbe-examples/any_type.bal");
    }

    @Test
    public void testAnydataType() {
        test("bbe-examples/anydata_type.bal");
    }

    @Test
    public void testArrayBindingPattern() {
        test("bbe-examples/array_binding_pattern.bal");
    }

    @Test
    public void testArrays() {
        test("bbe-examples/arrays.bal");
    }

    @Test
    public void testAsync() {
        test("bbe-examples/async.bal");
    }

    @Test
    public void testBallerinaToOpenapi() {
        test("bbe-examples/ballerina_to_openapi.bal");
    }

    @Test
    public void testBasicDocumentation() {
        test("bbe-examples/basic_documentation.bal");
    }

    @Test
    public void testBasicHttpsListener() {
        test("bbe-examples/basic_https_listener.bal");
    }

    @Test
    public void testBinaryBitwiseExpressions() {
        test("bbe-examples/binary_bitwise_expressions.bal");
    }

    @Test
    public void testByteIo() {
        test("bbe-examples/byte_io.bal");
    }

    @Test
    public void testByteType() {
        test("bbe-examples/byte_type.bal");
    }

    @Test
    public void testCache() {
        test("bbe-examples/cache.bal");
    }

    @Test
    public void testCharacterIo() {
        test("bbe-examples/character_io.bal");
    }

    @Test
    public void testCheck() {
        test("bbe-examples/check.bal");
    }

    @Test
    public void testCheckpanic() {
        test("bbe-examples/checkpanic.bal");
    }

    @Test
    public void testClassDefinition() {
        test("bbe-examples/class_definition.bal");
    }

    @Test
    public void testClone() {
        test("bbe-examples/clone.bal");
    }

    @Test
    public void testClosures() {
        test("bbe-examples/closures.bal");
    }

    @Test
    public void testCompoundAssignmentOperators() {
        test("bbe-examples/compound_assignment_operators.bal");
    }

    @Test
    public void testConfigApi() {
        test("bbe-examples/config_api.bal");
    }

    @Test
    public void testConstants() {
        test("bbe-examples/constants.bal");
    }

    @Test
    public void testContentBasedRouting() {
        test("bbe-examples/content_based_routing.bal");
    }

    @Test
    public void testCookieServer() {
        test("bbe-examples/cookie_server.bal");
    }

    @Test
    public void testCounterMetrics() {
        test("bbe-examples/counter_metrics.bal");
    }

    @Test
    public void testCreateJavaObjects() {
        test("bbe-examples/create_java_objects.bal");
    }

    @Test
    public void testCrypto() {
        test("bbe-examples/crypto.bal");
    }

    @Test
    public void testCsvIo() {
        test("bbe-examples/csv_io.bal");
    }

    @Test
    public void testDataIo() {
        test("bbe-examples/data_io.bal");
    }

    @Test
    public void testDeprecation() {
        test("bbe-examples/deprecation.bal");
    }

    @Test
    public void testDifferentPayloadTypes() {
        test("bbe-examples/different_payload_types.bal");
    }

    @Test
    public void testDirectoryListener() {
        test("bbe-examples/directory_listener.bal");
    }

    @Test
    public void testElvisOperator() {
        test("bbe-examples/elvis_operator.bal");
    }

    @Test
    public void testEnums() {
        test("bbe-examples/enums.bal");
    }

    @Test
    public void testEquality() {
        test("bbe-examples/equality.bal");
    }

    @Test
    public void testErrorDestructureBindingPattern() {
        test("bbe-examples/error_destructure_binding_pattern.bal");
    }

    @Test
    public void testErrorHandling() {
        test("bbe-examples/error_handling.bal");
    }

    @Test
    public void testErrorHandlingInSinglePlace() {
        test("bbe-examples/error_handling_in_single_place.bal");
    }

    @Test
    public void testExpressionBodiedFunctions() {
        test("bbe-examples/expression_bodied_functions.bal");
    }

    @Test
    public void testFail() {
        test("bbe-examples/fail.bal");
    }

    @Test
    public void testFile() {
        test("bbe-examples/file.bal");
    }

    @Test
    public void testFilepath() {
        test("bbe-examples/filepath.bal");
    }

    @Test
    public void testForeach() {
        test("bbe-examples/foreach.bal");
    }

    @Test
    public void testFork() {
        test("bbe-examples/fork.bal");
    }

    @Test
    public void testFunctionPointers() {
        test("bbe-examples/function_pointers.bal");
    }

    @Test
    public void testFunctionalIteration() {
        test("bbe-examples/functional_iteration.bal");
    }

    @Test
    public void testFunctions() {
        test("bbe-examples/functions.bal");
    }

    @Test
    public void testFunctionsWithDefaultableParameters() {
        test("bbe-examples/functions_with_defaultable_parameters.bal");
    }

    @Test
    public void testFunctionsWithRequiredParameters() {
        test("bbe-examples/functions_with_required_parameters.bal");
    }

    @Test
    public void testFunctionsWithRestParameter() {
        test("bbe-examples/functions_with_rest_parameter.bal");
    }

    @Test
    public void testGaugeMetrics() {
        test("bbe-examples/gauge_metrics.bal");
    }

    @Test
    public void testGrpcBidirectionalStreamingService() {
        test("bbe-examples/grpc_bidirectional_streaming_service.bal");
    }

    @Test
    public void testGrpcBidirectionalStreamingServiceClient() {
        test("bbe-examples/grpc_bidirectional_streaming_service_client.bal");
    }

    @Test
    public void testGrpcClientStreamingService() {
        test("bbe-examples/grpc_client_streaming_service.bal");
    }

    @Test
    public void testGrpcClientStreamingServiceClient() {
        test("bbe-examples/grpc_client_streaming_service_client.bal");
    }

    @Test
    public void testGrpcSecuredUnaryService() {
        test("bbe-examples/grpc_secured_unary_service.bal");
    }

    @Test
    public void testGrpcSecuredUnaryServiceClient() {
        test("bbe-examples/grpc_secured_unary_service_client.bal");
    }

    @Test
    public void testGrpcServerStreamingService() {
        test("bbe-examples/grpc_server_streaming_service.bal");
    }

    @Test
    public void testGrpcServerStreamingServiceClient() {
        test("bbe-examples/grpc_server_streaming_service_client.bal");
    }

    @Test
    public void testGrpcUnaryBlockingService() {
        test("bbe-examples/grpc_unary_blocking_service.bal");
    }

    @Test
    public void testGrpcUnaryBlockingServiceClient() {
        test("bbe-examples/grpc_unary_blocking_service_client.bal");
    }

    @Test
    public void testGrpcUnaryNonBlockingService() {
        test("bbe-examples/grpc_unary_non_blocking_service.bal");
    }

    @Test
    public void testHeaderBasedRouting() {
        test("bbe-examples/header_based_routing.bal");
    }

    @Test
    public void testHelloWorld() {
        test("bbe-examples/hello_world.bal");
    }

    @Test
    public void testHelloWorldClient() {
        test("bbe-examples/hello_world_client.bal");
    }

    @Test
    public void testHelloWorldParallel() {
        test("bbe-examples/hello_world_parallel.bal");
    }

    @Test
    public void testHelloWorldService() {
        test("bbe-examples/hello_world_service.bal");
    }

    @Test
    public void testHttp100Continue() {
        test("bbe-examples/http_100_continue.bal");
    }

    @Test
    public void testHttp11To20ProtocolSwitch() {
        test("bbe-examples/http_1_1_to_2_0_protocol_switch.bal");
    }

    @Test
    public void testHttp20Service() {
        test("bbe-examples/http_2_0_service.bal");
    }

    @Test
    public void testHttpAccessLogs() {
        test("bbe-examples/http_access_logs.bal");
    }

    @Test
    public void testHttpCachingClient() {
        test("bbe-examples/http_caching_client.bal");
    }

    @Test
    public void testHttpCircuitBreaker() {
        test("bbe-examples/http_circuit_breaker.bal");
    }

    @Test
    public void testHttpClient1() {
        test("bbe-examples/http_client_1.bal");
    }

    @Test
    public void testHttpClient2() {
        test("bbe-examples/http_client_2.bal");
    }

    @Test
    public void testHttpClientDataBinding() {
        test("bbe-examples/http_client_data_binding.bal");
    }

    @Test
    public void testHttpClientEndpoint() {
        test("bbe-examples/http_client_endpoint.bal");
    }

    @Test
    public void testHttpCompression() {
        test("bbe-examples/http_compression.bal");
    }

    @Test
    public void testHttpCors() {
        test("bbe-examples/http_cors.bal");
    }

    @Test
    public void testHttpDataBinding() {
        test("bbe-examples/http_data_binding.bal");
    }

    @Test
    public void testHttpDisableChunking() {
        test("bbe-examples/http_disable_chunking.bal");
    }

    @Test
    public void testHttpFailover() {
        test("bbe-examples/http_failover.bal");
    }

    @Test
    public void testHttpFilters() {
        test("bbe-examples/http_filters.bal");
    }

    @Test
    public void testHttpLoadBalancer() {
        test("bbe-examples/http_load_balancer.bal");
    }

    @Test
    public void testHttpRedirects() {
        test("bbe-examples/http_redirects.bal");
    }

    @Test
    public void testHttpRetry() {
        test("bbe-examples/http_retry.bal");
    }

    @Test
    public void testHttpStreaming() {
        test("bbe-examples/http_streaming.bal");
    }

    @Test
    public void testHttpTimeout() {
        test("bbe-examples/http_timeout.bal");
    }

    @Test
    public void testHttpTraceLogs() {
        test("bbe-examples/http_trace_logs.bal");
    }

    @Test
    public void testHttpsClient() {
        test("bbe-examples/https_client.bal");
    }

    @Test
    public void testHttpsListener() {
        test("bbe-examples/https_listener.bal");
    }

    @Test
    public void testHub() {
        test("bbe-examples/hub.bal");
    }

    @Test
    public void testIfElse() {
        test("bbe-examples/if_else.bal");
    }

    @Test
    public void testImmutableValues() {
        test("bbe-examples/immutable_values.bal");
    }

    @Test
    public void testInvokeJavaMethods() {
        test("bbe-examples/invoke_java_methods.bal");
    }

    @Test
    public void testIsolatedFunctions() {
        test("bbe-examples/isolated_functions.bal");
    }

    @Test
    public void testIterableObjects() {
        test("bbe-examples/iterable_objects.bal");
    }

    @Test
    public void testJavaArrays() {
        test("bbe-examples/java_arrays.bal");
    }

    @Test
    public void testJavaExceptions() {
        test("bbe-examples/java_exceptions.bal");
    }

    @Test
    public void testJavaVarargs() {
        test("bbe-examples/java_varargs.bal");
    }

    @Test
    public void testJdbcBatchExecuteOperation() {
        test("bbe-examples/jdbc_batch_execute_operation.bal");
    }

    @Test
    public void testJdbcComplexTypeQueries() {
        test("bbe-examples/jdbc_complex_type_queries.bal");
    }

    @Test
    public void testJdbcExecuteOperation() {
        test("bbe-examples/jdbc_execute_operation.bal");
    }

    @Test
    public void testJdbcInitOptions() {
        test("bbe-examples/jdbc_init_options.bal");
    }

    @Test
    public void testJdbcParameterizedQuery() {
        test("bbe-examples/jdbc_parameterized_query.bal");
    }

    @Test
    public void testJdbcQueryOperation() {
        test("bbe-examples/jdbc_query_operation.bal");
    }

    @Test
    public void testJson() {
        test("bbe-examples/json.bal");
    }

    @Test
    public void testJsonAccess() {
        test("bbe-examples/json_access.bal");
    }

    @Test
    public void testJsonArrays() {
        test("bbe-examples/json_arrays.bal");
    }

    @Test
    public void testJsonCsv() {
        test("bbe-examples/json_csv.bal");
    }

    @Test
    public void testJsonIo() {
        test("bbe-examples/json_io.bal");
    }

    @Test
    public void testJsonObjects() {
        test("bbe-examples/json_objects.bal");
    }

    @Test
    public void testJsonRecordMapConversion() {
        test("bbe-examples/json_record_map_conversion.bal");
    }

    @Test
    public void testJsonToXmlConversion() {
        test("bbe-examples/json_to_xml_conversion.bal");
    }

    @Test
    public void testJwtIssueValidate() {
        test("bbe-examples/jwt_issue_validate.bal");
    }

    @Test
    public void testJwtMockServer() {
        test("bbe-examples/jwt_mock_server.bal");
    }

    @Test
    public void testKafkaAuthenticationSaslPlainConsumer() {
        test("bbe-examples/kafka_authentication_sasl_plain_consumer.bal");
    }

    @Test
    public void testKafkaAuthenticationSaslPlainProducer() {
        test("bbe-examples/kafka_authentication_sasl_plain_producer.bal");
    }

    @Test
    public void testKafkaConsumerClient() {
        test("bbe-examples/kafka_consumer_client.bal");
    }

    @Test
    public void testKafkaConsumerGroupService() {
        test("bbe-examples/kafka_consumer_group_service.bal");
    }

    @Test
    public void testKafkaConsumerService() {
        test("bbe-examples/kafka_consumer_service.bal");
    }

    @Test
    public void testKafkaProducer() {
        test("bbe-examples/kafka_producer.bal");
    }

    @Test
    public void testKafkaProducerTransactional() {
        test("bbe-examples/kafka_producer_transactional.bal");
    }

    @Test
    public void testLength() {
        test("bbe-examples/length.bal");
    }

    @Test
    public void testLetExpression() {
        test("bbe-examples/let_expression.bal");
    }

    @Test
    public void testLocalTransactions() {
        test("bbe-examples/local_transactions.bal");
    }

    @Test
    public void testLocalTransactionsWithHandlers() {
        test("bbe-examples/local_transactions_with_handlers.bal");
    }

    @Test
    public void testLocks() {
        test("bbe-examples/locks.bal");
    }

    @Test
    public void testLogApi() {
        test("bbe-examples/log_api.bal");
    }

    @Test
    public void testMaps() {
        test("bbe-examples/maps.bal");
    }

    @Test
    public void testMatch() {
        test("bbe-examples/match.bal");
    }

    @Test
    public void testMathFunctions() {
        test("bbe-examples/math_functions.bal");
    }

    @Test
    public void testModules() {
        test("bbe-examples/modules.bal");
    }

    @Test
    public void testMutualSslService() {
        test("bbe-examples/mutual_ssl_service.bal");
    }

    @Test
    public void testMysqlBatchExecuteOperation() {
        test("bbe-examples/mysql_batch_execute_operation.bal");
    }

    @Test
    public void testMysqlCallStoredProcedures() {
        test("bbe-examples/mysql_call_stored_procedures.bal");
    }

    @Test
    public void testMysqlComplexTypeQueries() {
        test("bbe-examples/mysql_complex_type_queries.bal");
    }

    @Test
    public void testMysqlExecuteOperation() {
        test("bbe-examples/mysql_execute_operation.bal");
    }

    @Test
    public void testMysqlInitOptions() {
        test("bbe-examples/mysql_init_options.bal");
    }

    @Test
    public void testMysqlParameterizedQuery() {
        test("bbe-examples/mysql_parameterized_query.bal");
    }

    @Test
    public void testMysqlQueryOperation() {
        test("bbe-examples/mysql_query_operation.bal");
    }

    @Test
    public void testNeverType() {
        test("bbe-examples/never_type.bal");
    }

    @Test
    public void testNonTransactionRetry() {
        test("bbe-examples/non_transaction_retry.bal");
    }

    @Test
    public void testObjectAssignability() {
        test("bbe-examples/object_assignability.bal");
    }

    @Test
    public void testObjectConstructorExpression() {
        test("bbe-examples/object_constructor_expression.bal");
    }

    @Test
    public void testObjectFinalFields() {
        test("bbe-examples/object_final_fields.bal");
    }

    @Test
    public void testObjectInitializer() {
        test("bbe-examples/object_initializer.bal");
    }

    @Test
    public void testObjectMethods() {
        test("bbe-examples/object_methods.bal");
    }

    @Test
    public void testObjectType() {
        test("bbe-examples/object_type.bal");
    }

    @Test
    public void testObjectTypeReference() {
        test("bbe-examples/object_type_reference.bal");
    }

    @Test
    public void testOptionalFieldAccess() {
        test("bbe-examples/optional_field_access.bal");
    }

    @Test
    public void testOptionalType() {
        test("bbe-examples/optional_type.bal");
    }

    @Test
    public void testOrderMgmtService() {
        test("bbe-examples/order_mgmt_service.bal");
    }

    @Test
    public void testOverloadedMethodsConstructors() {
        test("bbe-examples/overloaded_methods_constructors.bal");
    }

    @Test
    public void testPanic() {
        test("bbe-examples/panic.bal");
    }

    @Test
    public void testPassthrough() {
        test("bbe-examples/passthrough.bal");
    }

    @Test
    public void testPublisher1() {
        test("bbe-examples/publisher_1.bal");
    }

    @Test
    public void testPublisher2() {
        test("bbe-examples/publisher_2.bal");
    }

    @Test
    public void testPublisher3() {
        test("bbe-examples/publisher_3.bal");
    }

    @Test
    public void testPublisher4() {
        test("bbe-examples/publisher_4.bal");
    }

    @Test
    public void testPublisher5() {
        test("bbe-examples/publisher_5.bal");
    }

    @Test
    public void testPublisher6() {
        test("bbe-examples/publisher_6.bal");
    }

    @Test
    public void testPublisher7() {
        test("bbe-examples/publisher_7.bal");
    }

    @Test
    public void testPublisher8() {
        test("bbe-examples/publisher_8.bal");
    }

    @Test
    public void testQueryAction() {
        test("bbe-examples/query_action.bal");
    }

    @Test
    public void testQueryExpression() {
        test("bbe-examples/query_expression.bal");
    }

    @Test
    public void testQueryExpressionToStream() {
        test("bbe-examples/query_expression_to_stream.bal");
    }

    @Test
    public void testQueryExpressionToString() {
        test("bbe-examples/query_expression_to_string.bal");
    }

    @Test
    public void testQueryExpressionToTable() {
        test("bbe-examples/query_expression_to_table.bal");
    }

    @Test
    public void testQueryExpressionToXml() {
        test("bbe-examples/query_expression_to_xml.bal");
    }

    @Test
    public void testQueryJoin() {
        test("bbe-examples/query_join.bal");
    }

    @Test
    public void testQueryPathMatrixParam() {
        test("bbe-examples/query_path_matrix_param.bal");
    }

    @Test
    public void testQueueGroup() {
        test("bbe-examples/queue-group.bal");
    }

    @Test
    public void testQuotedIdentifiers() {
        test("bbe-examples/quoted_identifiers.bal");
    }

    @Test
    public void testRabbitmqConsumer() {
        test("bbe-examples/rabbitmq_consumer.bal");
    }

    @Test
    public void testRabbitmqConsumerWithClientAcknowledgement() {
        test("bbe-examples/rabbitmq_consumer_with_client_acknowledgement.bal");
    }

    @Test
    public void testRabbitmqConsumerWithDataBinding() {
        test("bbe-examples/rabbitmq_consumer_with_data_binding.bal");
    }

    @Test
    public void testRabbitmqConsumerWithQosSettings() {
        test("bbe-examples/rabbitmq_consumer_with_qos_settings.bal");
    }

    @Test
    public void testRabbitmqProducer() {
        test("bbe-examples/rabbitmq_producer.bal");
    }

    @Test
    public void testRabbitmqTransactionConsumer() {
        test("bbe-examples/rabbitmq_transaction_consumer.bal");
    }

    @Test
    public void testRabbitmqTransactionProducer() {
        test("bbe-examples/rabbitmq_transaction_producer.bal");
    }

    @Test
    public void testRangeExpressions() {
        test("bbe-examples/range_expressions.bal");
    }

    @Test
    public void testRawTemplate() {
        test("bbe-examples/raw_template.bal");
    }

    @Test
    public void testReadonlyObjects() {
        test("bbe-examples/readonly_objects.bal");
    }

    @Test
    public void testReadonlyType() {
        test("bbe-examples/readonly_type.bal");
    }

    @Test
    public void testRecordBindingPattern() {
        test("bbe-examples/record_binding_pattern.bal");
    }

    @Test
    public void testRecordIo() {
        test("bbe-examples/record_io.bal");
    }

    @Test
    public void testRecordOptionalFields() {
        test("bbe-examples/record_optional_fields.bal");
    }

    @Test
    public void testRecordReadonlyFields() {
        test("bbe-examples/record_readonly_fields.bal");
    }

    @Test
    public void testRecordTypeReference() {
        test("bbe-examples/record_type_reference.bal");
    }

    @Test
    public void testRecords() {
        test("bbe-examples/records.bal");
    }

    @Test
    public void testRequestWithMultiparts() {
        test("bbe-examples/request_with_multiparts.bal");
    }

    @Test
    public void testResponseWithMultiparts() {
        test("bbe-examples/response_with_multiparts.bal");
    }

    @Test
    public void testRestrictByMediaType() {
        test("bbe-examples/restrict_by_media_type.bal");
    }

    @Test
    public void testRetryTransactions() {
        test("bbe-examples/retry_transactions.bal");
    }

    @Test
    public void testSecuredClientWithBasicAuth() {
        test("bbe-examples/secured_client_with_basic_auth.bal");
    }

    @Test
    public void testSecuredClientWithJwtAuth() {
        test("bbe-examples/secured_client_with_jwt_auth.bal");
    }

    @Test
    public void testSecuredClientWithOauth2() {
        test("bbe-examples/secured_client_with_oauth2.bal");
    }

    @Test
    public void testSecuredServiceWithBasicAuth1() {
        test("bbe-examples/secured_service_with_basic_auth_1.bal");
    }

    @Test
    public void testSecuredServiceWithBasicAuth2() {
        test("bbe-examples/secured_service_with_basic_auth_2.bal");
    }

    @Test
    public void testSecuredServiceWithJwtAuth1() {
        test("bbe-examples/secured_service_with_jwt_auth_1.bal");
    }

    @Test
    public void testSecuredServiceWithJwtAuth2() {
        test("bbe-examples/secured_service_with_jwt_auth_2.bal");
    }

    @Test
    public void testSecuredServiceWithLdap() {
        test("bbe-examples/secured_service_with_ldap.bal");
    }

    @Test
    public void testSecuredServiceWithOauth2() {
        test("bbe-examples/secured_service_with_oauth2.bal");
    }

    @Test
    public void testSendAndReceiveEmails() {
        test("bbe-examples/send_and_receive_emails.bal");
    }

    @Test
    public void testShiftExpressions() {
        test("bbe-examples/shift_expressions.bal");
    }

    @Test
    public void testSslClient() {
        test("bbe-examples/ssl_client.bal");
    }

    @Test
    public void testStreams() {
        test("bbe-examples/streams.bal");
    }

    @Test
    public void testStringTemplate() {
        test("bbe-examples/string_template.bal");
    }

    @Test
    public void testStrings() {
        test("bbe-examples/strings.bal");
    }

    @Test
    public void testSubscriber1() {
        test("bbe-examples/subscriber_1.bal");
    }

    @Test
    public void testSubscriber2() {
        test("bbe-examples/subscriber_2.bal");
    }

    @Test
    public void testSubscriber3() {
        test("bbe-examples/subscriber_3.bal");
    }

    @Test
    public void testSubscriber4() {
        test("bbe-examples/subscriber_4.bal");
    }

    @Test
    public void testSubscriber5() {
        test("bbe-examples/subscriber_5.bal");
    }

    @Test
    public void testSubscriber6() {
        test("bbe-examples/subscriber_6.bal");
    }

    @Test
    public void testSubscriber7() {
        test("bbe-examples/subscriber_7.bal");
    }

    @Test
    public void testSubscriber8() {
        test("bbe-examples/subscriber_8.bal");
    }

    @Test
    public void testSubscriberService() {
        test("bbe-examples/subscriber_service.bal");
    }

    @Test
    public void testSubscriptionChangeClient() {
        test("bbe-examples/subscription_change_client.bal");
    }

    @Test
    public void testTable() {
        test("bbe-examples/table.bal");
    }

    @Test
    public void testTaintChecking() {
        test("bbe-examples/taint_checking.bal");
    }

    @Test
    public void testTaskSchedulerAppointment() {
        test("bbe-examples/task_scheduler_appointment.bal");
    }

    @Test
    public void testTaskSchedulerTimer() {
        test("bbe-examples/task_scheduler_timer.bal");
    }

    @Test
    public void testTaskServiceAppointment() {
        test("bbe-examples/task_service_appointment.bal");
    }

    @Test
    public void testTaskServiceTimer() {
        test("bbe-examples/task_service_timer.bal");
    }

    @Test
    public void testTcpSocketClient() {
        test("bbe-examples/tcp_socket_client.bal");
    }

    @Test
    public void testTcpSocketListener() {
        test("bbe-examples/tcp_socket_listener.bal");
    }

    @Test
    public void testTheMainFunction() {
        test("bbe-examples/the_main_function.bal");
    }

    @Test
    public void testThreadsAndStrands() {
        test("bbe-examples/threads_and_strands.bal");
    }

    @Test
    public void testTime() {
        test("bbe-examples/time.bal");
    }

    @Test
    public void testTracing() {
        test("bbe-examples/tracing.bal");
    }

    @Test
    public void testTrap() {
        test("bbe-examples/trap.bal");
    }

    @Test
    public void testTupleBindingPattern() {
        test("bbe-examples/tuple_binding_pattern.bal");
    }

    @Test
    public void testTupleType() {
        test("bbe-examples/tuple_type.bal");
    }

    @Test
    public void testTypeCast() {
        test("bbe-examples/type_cast.bal");
    }

    @Test
    public void testTypeConversion() {
        test("bbe-examples/type_conversion.bal");
    }

    @Test
    public void testTypeGuard() {
        test("bbe-examples/type_guard.bal");
    }

    @Test
    public void testUdpSocketClient() {
        test("bbe-examples/udp_socket_client.bal");
    }

    @Test
    public void testUnionType() {
        test("bbe-examples/union_type.bal");
    }

    @Test
    public void testUrlEncodeDecode() {
        test("bbe-examples/url_encode_decode.bal");
    }

    @Test
    public void testUserDefinedError() {
        test("bbe-examples/user_defined_error.bal");
    }

    @Test
    public void testValues() {
        test("bbe-examples/values.bal");
    }

    @Test
    public void testVar() {
        test("bbe-examples/var.bal");
    }

    @Test
    public void testVariables() {
        test("bbe-examples/variables.bal");
    }

    @Test
    public void testWebsocketBasicSample() {
        test("bbe-examples/websocket_basic_sample.bal");
    }

    @Test
    public void testWebsocketChatApplication() {
        test("bbe-examples/websocket_chat_application.bal");
    }

    @Test
    public void testWebsocketProxyServer() {
        test("bbe-examples/websocket_proxy_server.bal");
    }

    @Test
    public void testWebsocketService() {
        test("bbe-examples/websocket_service.bal");
    }

    @Test
    public void testWebsocketService9094() {
        test("bbe-examples/websocket_service_9094.bal");
    }

    @Test
    public void testWebsocketService9095() {
        test("bbe-examples/websocket_service_9095.bal");
    }

    @Test
    public void testWhile() {
        test("bbe-examples/while.bal");
    }

    @Test
    public void testWorkerInteraction() {
        test("bbe-examples/worker_interaction.bal");
    }

    @Test
    public void testWorkers() {
        test("bbe-examples/workers.bal");
    }

    @Test
    public void testXml() {
        test("bbe-examples/xml.bal");
    }

    @Test
    public void testXmlAccess() {
        test("bbe-examples/xml_access.bal");
    }

    @Test
    public void testXmlAttributes() {
        test("bbe-examples/xml_attributes.bal");
    }

    @Test
    public void testXmlFunctions() {
        test("bbe-examples/xml_functions.bal");
    }

    @Test
    public void testXmlIo() {
        test("bbe-examples/xml_io.bal");
    }

    @Test
    public void testXmlLiteral() {
        test("bbe-examples/xml_literal.bal");
    }

    @Test
    public void testXmlNamespaces() {
        test("bbe-examples/xml_namespaces.bal");
    }

    @Test
    public void testXsltTransformation() {
        test("bbe-examples/xslt_transformation.bal");
    }
}
