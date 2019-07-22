/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.net.http;

/**
 * Defines enum for Http Error types. This enum stores Error type name and corresponding reason string.
 */
public enum HttpErrorType {
    // ResiliencyError
    FAILOVER_ALL_ENDPOINTS_FAILED("FailoverAllEndpointsFailed", "{ballerina/http}FailoverAllEndpointsFailed"),
    FAILOVER_ENDPOINT_ACTION_FAILED("FailoverActionFailedError", "{ballerina/http}FailoverEndpointActionFailed"),
    UPSTREAM_SERVICE_UNAVAILABLE("UpstreamServiceUnavailableError", "{ballerina/http}UpstreamServiceUnavailable"),
    ALL_LOAD_BALANCE_ENDPOINTS_FAILED("AllLoadBalanceEndpointsFailedError",
            "{ballerina/http}AllLoadBalanceEndpointsFailed"),
    ALL_RETRY_ATTEMPTS_FAILED("AllRetryAttemptsFailed", "{ballerina/http}AllRetryAttemptsFailed"),
    IDLE_TIMEOUT_TRIGGERED("IdleTimeoutError", "{ballerina/http}IdleTimeoutError"),
    LISTENER_STARTUP_FAILURE("ListenerError", "{ballerina/http}ListenerStartupError"),

    // ClientAuthError
    AUTHN_FAILED("AuthenticationError", "{ballerina/http}AuthenticationFailed"),
    AUTHZ_FAILED("AuthorizationError", "{ballerina/http}AuthorizationFailed"),

    // OutboundRequestError (Client)
    INIT_OUTBOUND_REQUEST_FAILED("InitializingOutboundRequestError",
            "{ballerina/http}InitializingOutboundRequestFailed"),
    WRITING_OUTBOUND_REQUEST_HEADER_FAILED("WritingOutboundRequestHeaderError",
            "{ballerina/http}WritingOutboundRequestHeaderFailed"),
    WRITING_OUTBOUND_REQUEST_BODY_FAILED("WritingOutboundRequestBodyError",
            "{ballerina/http}WritingOutboundRequestBodyFailed"),

    // InboundResponseError (Client)
    INIT_INBOUND_RESPONSE_FAILED("{ballerina/http}InitializingInboundResponseFailed",
            "InitializingInboundResponseError"),
    READING_INBOUND_RESPONSE_HEADERS_FAILED("ReadingInboundResponseHeadersError",
            "{ballerina/http}ReadingInboundResponseHeadersFailed"),
    READING_INBOUND_RESPONSE_BODY_FAILED("ReadingInboundResponseBodyError",
            "{ballerina/http}ReadingInboundResponseBodyFailed"),

    // InboundRequestError
    INIT_INBOUND_REQUEST_FAILED("InitializingInboundRequestError",
            "{ballerina/http}InitializingInboundRequestFailed"),
    READING_INBOUND_REQUEST_HEADER_FAILED("ReadingInboundRequestHeaderError",
            "{ballerina/http}ReadingInboundRequestHeaderFailed"),
    READING_INBOUND_REQUEST_BODY_FAILED("ReadingInboundRequestBodyError",
            "{ballerina/http}ReadingInboundRequestBodyFailed"),

    // OutboundResponseError
    INIT_OUTBOUND_RESPONSE_FAILED("{ballerina/http}InitializingOutboundResponseFailed",
            "InitializingInboundResponseError"),
    WRITING_OUTBOUND_RESPONSE_HEADERS_FAILED("WritingOutboundResponseHeadersError",
            "{ballerina/http}WritingOutboundResponseHeadersFailed"),
    WRITING_OUTBOUND_RESPONSE_BODY_FAILED("WritingOutboundResponseBodyError",
            "{ballerina/http}WritingOutboundResponseBodyFailed"),
    INIT_100_CONTINUE_RESPONSE_FAILED("Initiating100ContinueResponseError",
            "{ballerina/http}Initializing100ContinueResponseFailed"),
    WRITING_100_CONTINUE_RESPONSE_FAILED("Writing100ContinueResponseError",
            "{ballerina/http}Writing100ContinueResponseFailed"),

    // Other errors
    GENERIC_CLIENT_ERROR("GenericClientError", "{ballerina/http}GenericClientError"),
    GENERIC_LISTENER_ERROR("GenericListenerError", "{ballerina/http}GenericListenerError"),
    UNSUPPORTED_ACTION("UnsupportedActionError", "{ballerina/http}UnsupportedAction"),
    HTTP2_CLIENT_ERROR("Http2ClientError", "{ballerina/http}Http2ClientError"),
    MAXIMUM_WAIT_TIME_EXCEEDED("MaximumWaitTimeExceededError", "{ballerina/http}MaximumWaitTimeExceeded"),
    SSL_ERROR("SslError", "{ballerina/http}SslError");


    private final String errorName;
    private final String reason;

    HttpErrorType(String errorName, String reason) {
        this.errorName = errorName;
        this.reason = reason;
    }

    /**
     * Returns the name of the error type, which is defined in the ballerina http errors.
     *
     * @return the name of the error type as a String
     */
    public String getErrorName() {
        return errorName;
    }

    /**
     * Returns the reason string of the error, as defined in the ballerina errors.
     *
     * @return the reason constant value of the error, as a String
     */
    public String getReason() {
        return reason;
    }
}
