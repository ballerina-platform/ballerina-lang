// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

# Holds the details of an HTTP error
#
# + message - Specific error message for the error
# + cause - Cause of the error; If this error occurred due to another error (Probably from another module)
public type Detail record {
    string message;
    error cause?;
};

// Ballerina HTTP Client Error Types

# Resiliency errors
public const FAILOVER_ALL_ENDPOINTS_FAILED = "{ballerina/http}FailoverAllEndpointsFailed";
public type FailoverAllEndpointsFailedError error<FAILOVER_ALL_ENDPOINTS_FAILED, Detail>;

public const FAILOVER_ENDPOINT_ACTION_FAILED = "{ballerina/http}FailoverEndpointActionFailed";
public type FailoverActionFailedError error<FAILOVER_ENDPOINT_ACTION_FAILED, Detail>;

public const UPSTREAM_SERVICE_UNAVAILABLE = "{ballerina/http}UpstreamServiceUnavailable";
public type UpstreamServiceUnavailableError error<UPSTREAM_SERVICE_UNAVAILABLE, Detail>;

public const ALL_LOAD_BALANCE_ENDPOINTS_FAILED = "{ballerina/http}AllLoadBalanceEndpointsFailed";
public type AllLoadBalanceEndpointsFailedError error<ALL_LOAD_BALANCE_ENDPOINTS_FAILED, Detail>;

public const ALL_RETRY_ATTEMPTS_FAILED = "{ballerina/http}AllRetryAttemptsFailed";
public type AllRetryAttemptsFailed error<ALL_RETRY_ATTEMPTS_FAILED, Detail>;

public const IDLE_TIMEOUT_TRIGGERED = "{ballerina/http}IdleTimeoutError";
public type IdleTimeoutError error<IDLE_TIMEOUT_TRIGGERED, Detail>;

# Security errors
public const AUTHN_FAILED = "{ballerina/http}AuthenticationFailed";
public type AuthenticationError error<AUTHN_FAILED, Detail>;

public const AUTHZ_FAILED = "{ballerina/http}AuthorizationFailed";
public type AuthorizationError error<AUTHZ_FAILED, Detail>;

# Outbound request errors in client
public const INIT_OUTBOUND_REQUEST_FAILED = "{ballerina/http}InitializingOutboundRequestFailed";
public type InitializingOutboundRequestError error<INIT_OUTBOUND_REQUEST_FAILED, Detail>;

public const WRITING_OUTBOUND_REQUEST_HEADERS_FAILED = "{ballerina/http}WritingOutboundRequestHeadersFailed";
public type WritingOutboundRequestHeadersError error<WRITING_OUTBOUND_REQUEST_HEADERS_FAILED, Detail>;

public const WRITING_OUTBOUND_REQUEST_BODY_FAILED = "{ballerina/http}WritingOutboundRequestBodyFailed";
public type WritingOutboundRequestBodyError error<WRITING_OUTBOUND_REQUEST_BODY_FAILED, Detail>;

# Inbound response errors in client
public const INIT_INBOUND_RESPONSE_FAILED = "{ballerina/http}InitializingInboundResponseFailed";
public type InitializingInboundResponseError error<INIT_INBOUND_RESPONSE_FAILED, Detail>;

public const READING_INBOUND_RESPONSE_HEADERS_FAILED = "{ballerina/http}ReadingInboundResponseHeadersFailed";
public type ReadingInboundResponseHeadersError error<READING_INBOUND_RESPONSE_HEADERS_FAILED, Detail>;

public const READING_INBOUND_RESPONSE_BODY_FAILED = "{ballerina/http}ReadingInboundResponseBodyFailed";
public type ReadingInboundResponseBodyError error<READING_INBOUND_RESPONSE_BODY_FAILED, Detail>;

# Inbound request errors in listener
public const INIT_INBOUND_REQUEST_FAILED = "{ballerina/http}InitializingInboundRequestFailed";
public type InitializingInboundRequestError error<INIT_INBOUND_REQUEST_FAILED, Detail>;

public const READING_INBOUND_REQUEST_HEADERS_FAILED = "{ballerina/http}ReadingInboundRequestHeadersFailed";
public type ReadingInboundRequestHeadersError error<READING_INBOUND_REQUEST_HEADERS_FAILED, Detail>;

public const READING_INBOUND_REQUEST_BODY_FAILED = "{ballerina/http}ReadingInboundRequestBodyFailed";
public type ReadingInboundRequestBodyError error<READING_INBOUND_REQUEST_BODY_FAILED, Detail>;

# Outbound response errors in listener
public const INIT_OUTBOUND_RESPONSE_FAILED = "{ballerina/http}InitializingOutboundResponseFailed";
public type InitializingOutboundResponseError error<INIT_OUTBOUND_RESPONSE_FAILED, Detail>;

public const WRITING_OUTBOUND_RESPONSE_HEADERS_FAILED = "{ballerina/http}WritingOutboundResponseHeadersFailed";
public type WritingOutboundResponseHeadersError error<WRITING_OUTBOUND_RESPONSE_HEADERS_FAILED, Detail>;

public const WRITING_OUTBOUND_RESPONSE_BODY_FAILED = "{ballerina/http}WritingOutboundResponseBodyFailed";
public type WritingOutboundResponseBodyError error<WRITING_OUTBOUND_RESPONSE_BODY_FAILED, Detail>;

public const INITIATING_100_CONTINUE_RESPONSE_FAILED = "{ballerina/http}Initializing100ContinueResponseFailed";
public type Initiating100ContinueResponseError error<INITIATING_100_CONTINUE_RESPONSE_FAILED, Detail>;

public const WRITING_100_CONTINUE_RESPONSE_FAILED = "{ballerina/http}Writing100ContinueResponseFailed";
public type Writing100ContinueResponseError error<WRITING_100_CONTINUE_RESPONSE_FAILED, Detail>;

# Generic errors (mostly to wrap errors from other modules)
public const GENERIC_CLIENT_ERROR = "{ballerina/http}GenericClientError";
public type GenericClientError error<GENERIC_CLIENT_ERROR, Detail>;

public const GENERIC_LISTENER_ERROR = "{ballerina/http}GenericListenerError";
public type GenericListenerError error<GENERIC_LISTENER_ERROR, Detail>;

# Other client-related errors
public const UNSUPPORTED_ACTION = "{ballerina/http}UnsupportedAction";
public type UnsupportedActionError error<UNSUPPORTED_ACTION, Detail>;

public const HTTP2_CLIENT_ERROR = "{ballerina/http}Http2ClientError";
public type Http2ClientError error<HTTP2_CLIENT_ERROR, Detail>;

public const MAXIMUM_WAIT_TIME_EXCEEDED = "{ballerina/http}MaximumWaitTimeExceeded";
public type MaximumWaitTimeExceededError error<MAXIMUM_WAIT_TIME_EXCEEDED, Detail>;

public const SSL_ERROR = "{ballerina/http}SslError";
public type SslError error<SSL_ERROR, Detail>;

# Ballerina Http Union Errors
public type ResiliencyError FailoverAllEndpointsFailedError|FailoverActionFailedError|
                            UpstreamServiceUnavailableError|AllLoadBalanceEndpointsFailedError|AllRetryAttemptsFailed|
                            IdleTimeoutError;

public type ClientAuthError AuthenticationError|AuthorizationError;

public type OutboundRequestError InitializingOutboundRequestError|WritingOutboundRequestHeadersError|
                            WritingOutboundRequestBodyError;

public type InboundResponseError InitializingInboundResponseError|ReadingInboundResponseHeadersError|
                            ReadingInboundResponseBodyError;

public type InboundRequestError InitializingInboundRequestError|ReadingInboundRequestHeadersError|
                            ReadingInboundRequestBodyError;

public type OutboundResponseError InitializingOutboundResponseError|WritingOutboundResponseHeadersError|
                            WritingOutboundResponseBodyError|Initiating100ContinueResponseError|
                            Writing100ContinueResponseError;

public type ClientError ResiliencyError|ClientAuthError|OutboundRequestError|
                            InboundResponseError|UnsupportedActionError|Http2ClientError|
                            MaximumWaitTimeExceededError|SslError|GenericClientError;

public type ListenerError GenericListenerError|InboundRequestError|OutboundResponseError;

function getGenericClientError(string message, error cause) returns GenericClientError {
    GenericClientError err = error(GENERIC_CLIENT_ERROR, message = message, cause = cause);
    return err;
}
