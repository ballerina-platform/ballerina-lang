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
# + statusCode - The status code, if the inbound error response exists
public type Detail record {
    string message;
    error cause?;
    int statusCode?;
};

// Ballerina HTTP Client Error Types

// Resiliency errors
# Represents the reason string for the `http:FailoverAllEndpointsFailedError`
public const FAILOVER_ALL_ENDPOINTS_FAILED = "{ballerina/http}FailoverAllEndpointsFailed";
# Represents a client error that occurred due to all the failover endpoint failure
public type FailoverAllEndpointsFailedError error<FAILOVER_ALL_ENDPOINTS_FAILED, Detail>;

# Represents the reason string for the `http:FailoverActionFailedError`
public const FAILOVER_ENDPOINT_ACTION_FAILED = "{ballerina/http}FailoverEndpointActionFailed";
# Represents a client error that occurred due to failover action failure
public type FailoverActionFailedError error<FAILOVER_ENDPOINT_ACTION_FAILED, Detail>;

# Represents the reason string for the `http:UpstreamServiceUnavailableError`
public const UPSTREAM_SERVICE_UNAVAILABLE = "{ballerina/http}UpstreamServiceUnavailable";
# Represents a client error that occurred due to upstream service unavailability
public type UpstreamServiceUnavailableError error<UPSTREAM_SERVICE_UNAVAILABLE, Detail>;

# Represents the reason string for the `http:AllLoadBalanceEndpointsFailedError`
public const ALL_LOAD_BALANCE_ENDPOINTS_FAILED = "{ballerina/http}AllLoadBalanceEndpointsFailed";
# Represents a client error that occurred due to all the load balance endpoint failure
public type AllLoadBalanceEndpointsFailedError error<ALL_LOAD_BALANCE_ENDPOINTS_FAILED, Detail>;

# Represents the reason string for the `http:AllRetryAttemptsFailed`
public const ALL_RETRY_ATTEMPTS_FAILED = "{ballerina/http}AllRetryAttemptsFailed";
# Represents a client error that occurred due to all the the retry attempts failure
public type AllRetryAttemptsFailed error<ALL_RETRY_ATTEMPTS_FAILED, Detail>;

# Represents the reason string for the `http:IdleTimeoutError`
public const IDLE_TIMEOUT_TRIGGERED = "{ballerina/http}IdleTimeoutError";
# Represents the error that triggered upon a request/response idle timeout
public type IdleTimeoutError error<IDLE_TIMEOUT_TRIGGERED, Detail>;

// Security errors
# Represents the reason string for the `http:AuthenticationError`
public const AUTHN_FAILED = "{ballerina/http}AuthenticationFailed";
# Represents a listener error that occurred due to inbound request authentication failure
public type AuthenticationError error<AUTHN_FAILED, Detail>;

# Represents the reason string for the `http:AuthorizationError`
public const AUTHZ_FAILED = "{ballerina/http}AuthorizationFailed";
# Represents a listener error that occurred due to inbound request authorization failure
public type AuthorizationError error<AUTHZ_FAILED, Detail>;

// Outbound request errors in client
# Represents the reason string for the `http:InitializingOutboundRequestError`
public const INIT_OUTBOUND_REQUEST_FAILED = "{ballerina/http}InitializingOutboundRequestFailed";
# Represents a client error that occurred due to outbound request initialization failure
public type InitializingOutboundRequestError error<INIT_OUTBOUND_REQUEST_FAILED, Detail>;

# Represents the reason string for the `http:WritingOutboundRequestHeadersError`
public const WRITING_OUTBOUND_REQUEST_HEADERS_FAILED = "{ballerina/http}WritingOutboundRequestHeadersFailed";
# Represents a client error that occurred while writing outbound request headers
public type WritingOutboundRequestHeadersError error<WRITING_OUTBOUND_REQUEST_HEADERS_FAILED, Detail>;

# Represents the reason string for the `http:WritingOutboundRequestBodyError`
public const WRITING_OUTBOUND_REQUEST_BODY_FAILED = "{ballerina/http}WritingOutboundRequestBodyFailed";
# Represents a client error that occurred while writing outbound request entity body
public type WritingOutboundRequestBodyError error<WRITING_OUTBOUND_REQUEST_BODY_FAILED, Detail>;

// Inbound response errors in client
# Represents the reason string for the `http:InitializingInboundResponseError`
public const INIT_INBOUND_RESPONSE_FAILED = "{ballerina/http}InitializingInboundResponseFailed";
# Represents a client error that occurred due to inbound response initialization failure
public type InitializingInboundResponseError error<INIT_INBOUND_RESPONSE_FAILED, Detail>;

# Represents the reason string for the `http:ReadingInboundResponseBodyError`
public const READING_INBOUND_RESPONSE_HEADERS_FAILED = "{ballerina/http}ReadingInboundResponseHeadersFailed";
# Represents a client error that occurred while reading inbound response headers
public type ReadingInboundResponseHeadersError error<READING_INBOUND_RESPONSE_HEADERS_FAILED, Detail>;

# Represents the reason string for the `http:ReadingInboundResponseBodyError`
public const READING_INBOUND_RESPONSE_BODY_FAILED = "{ballerina/http}ReadingInboundResponseBodyFailed";
# Represents a client error that occurred while reading inbound response entity body
public type ReadingInboundResponseBodyError error<READING_INBOUND_RESPONSE_BODY_FAILED, Detail>;

//Inbound request errors in listener
# Represents the reason string for the `http:InitialingInboundRequestError`
public const INIT_INBOUND_REQUEST_FAILED = "{ballerina/http}InitializingInboundRequestFailed";
# Represents a listener error that occurred due to inbound request initialization failure
public type InitializingInboundRequestError error<INIT_INBOUND_REQUEST_FAILED, Detail>;

# Represents the reason string for the `http:ReadingInboundRequestHeadersError`
public const READING_INBOUND_REQUEST_HEADERS_FAILED = "{ballerina/http}ReadingInboundRequestHeadersFailed";
# Represents a listener error that occurred while reading inbound request headers
public type ReadingInboundRequestHeadersError error<READING_INBOUND_REQUEST_HEADERS_FAILED, Detail>;

# Represents the reason string for the `http:ReadingInboundRequestBodyError`
public const READING_INBOUND_REQUEST_BODY_FAILED = "{ballerina/http}ReadingInboundRequestBodyFailed";
# Represents a listener error that occurred while writing the inbound request entity body
public type ReadingInboundRequestBodyError error<READING_INBOUND_REQUEST_BODY_FAILED, Detail>;

// Outbound response errors in listener
# Represents the reason string for the `http:InitializingOutboundResponseError`
public const INIT_OUTBOUND_RESPONSE_FAILED = "{ballerina/http}InitializingOutboundResponseFailed";
# Represents a listener error that occurred due to outbound response initialization failure
public type InitializingOutboundResponseError error<INIT_OUTBOUND_RESPONSE_FAILED, Detail>;

# Represents the reason string for the `http:WritingOutboundResponseHeadersError`
public const WRITING_OUTBOUND_RESPONSE_HEADERS_FAILED = "{ballerina/http}WritingOutboundResponseHeadersFailed";
# Represents a listener error that occurred while writing outbound response headers
public type WritingOutboundResponseHeadersError error<WRITING_OUTBOUND_RESPONSE_HEADERS_FAILED, Detail>;

# Represents the reason string for the `http:WritingOutboundResponseBodyError`
public const WRITING_OUTBOUND_RESPONSE_BODY_FAILED = "{ballerina/http}WritingOutboundResponseBodyFailed";
# Represents a listener error that occurred while writing outbound response entity body
public type WritingOutboundResponseBodyError error<WRITING_OUTBOUND_RESPONSE_BODY_FAILED, Detail>;

# Represents the reason string for the `http:Initiating100ContinueResponseError`
public const INITIATING_100_CONTINUE_RESPONSE_FAILED = "{ballerina/http}Initializing100ContinueResponseFailed";
# Represents an error that occurred due to 100 continue response initialization failure
public type Initiating100ContinueResponseError error<INITIATING_100_CONTINUE_RESPONSE_FAILED, Detail>;

# Represents the reason string for the `http:Writing100ContinueResponseError`
public const WRITING_100_CONTINUE_RESPONSE_FAILED = "{ballerina/http}Writing100ContinueResponseFailed";
# Represents an error that occurred while writing 100 continue response
public type Writing100ContinueResponseError error<WRITING_100_CONTINUE_RESPONSE_FAILED, Detail>;

# Represents the reason string for the `http:InvalidCookieError`
public const INVALID_COOKIE_ERROR = "{ballerina/http}InvalidCookieError";
# Represents a cookie error that occurred when sending cookies in the response
public type InvalidCookieError error<INVALID_COOKIE_ERROR, Detail>;

// Generic errors (mostly to wrap errors from other modules)
# Error reason for generic client error
public const GENERIC_CLIENT_ERROR = "{ballerina/http}GenericClientError";
# Represents a generic client error
public type GenericClientError error<GENERIC_CLIENT_ERROR, Detail>;

# Represents the reason string for the `http:GenericListenerError`
public const GENERIC_LISTENER_ERROR = "{ballerina/http}GenericListenerError";
# Represents a generic listener error
public type GenericListenerError error<GENERIC_LISTENER_ERROR, Detail>;

// Other client-related errors
# Represents the reason string for the `http:UnsupportedActionError`
public const UNSUPPORTED_ACTION = "{ballerina/http}UnsupportedAction";
# Represents a client error that occurred due to unsupported action invocation
public type UnsupportedActionError error<UNSUPPORTED_ACTION, Detail>;

# Represents the reason string for the `http:Http2ClientError`
public const HTTP2_CLIENT_ERROR = "{ballerina/http}Http2ClientError";
# Represents an HTTP/2 client generic error
public type Http2ClientError error<HTTP2_CLIENT_ERROR, Detail>;

# Represents the reason string for the `http:MaximumWaitTimeExceededError`
public const MAXIMUM_WAIT_TIME_EXCEEDED = "{ballerina/http}MaximumWaitTimeExceeded";
# Represents a client error that occurred exceeding maximum wait time
public type MaximumWaitTimeExceededError error<MAXIMUM_WAIT_TIME_EXCEEDED, Detail>;

# Represents the reason string for the `http:SslError`
public const SSL_ERROR = "{ballerina/http}SslError";
# Represents a client error that occurred due to SSL failure
public type SslError error<SSL_ERROR, Detail>;

# Represents the reason string for the `http:CookieHandlingError`
public const COOKIE_HANDLING_ERROR = "{ballerina/http}CookieHandlingError";
# Represents a cookie error that occurred when using the cookies
public type CookieHandlingError error<COOKIE_HANDLING_ERROR, Detail>;

# Represents the reason string for the `http:IllegalDataBindingStateError`
public const ILLEGAL_DATA_BINDING_STATE_ERROR = "{ballerina/http}IllegalDataBindingStateError";
# Represents a data binding illegal state error
public type IllegalDataBindingStateError error<ILLEGAL_DATA_BINDING_STATE_ERROR, Detail>;

# Represents the reason string for the `http:ClientRequestError`
public const CLIENT_REQUEST_ERROR = "{ballerina/http}ClientRequestError";
# Represents an error that occurred due to bad syntax or incomplete info of client request(4xx HTTP response)
public type ClientRequestError error<CLIENT_REQUEST_ERROR, Detail>;

# Represents the reason string for the `http:RemoteServerError`
public const REMOTE_SERVER_ERROR = "{ballerina/http}RemoteServerError";
# Represents an error that occurred due to a failure of the remote server(5xx HTTP response)
public type RemoteServerError error<REMOTE_SERVER_ERROR, Detail>;

// Ballerina HTTP Union Errors
# Defines the resiliency error types that returned from client
public type ResiliencyError FailoverAllEndpointsFailedError | FailoverActionFailedError |
                            UpstreamServiceUnavailableError | AllLoadBalanceEndpointsFailedError |
                            AllRetryAttemptsFailed | IdleTimeoutError;

# Defines the Auth error types that returned from client
public type ClientAuthError AuthenticationError|AuthorizationError;

# Defines the client error types that returned while sending outbound request
public type OutboundRequestError InitializingOutboundRequestError|WritingOutboundRequestHeadersError|
                            WritingOutboundRequestBodyError;

# Defines the client error types that returned while receiving inbound response
public type InboundResponseError InitializingInboundResponseError|ReadingInboundResponseHeadersError|
                            ReadingInboundResponseBodyError;

# Defines the listener error types that returned while receiving inbound request
public type InboundRequestError InitializingInboundRequestError|ReadingInboundRequestHeadersError|
                            ReadingInboundRequestBodyError;

# Defines the listener error types that returned while sending outbound response
public type OutboundResponseError InitializingOutboundResponseError|WritingOutboundResponseHeadersError|
                            WritingOutboundResponseBodyError|Initiating100ContinueResponseError|
                            Writing100ContinueResponseError|InvalidCookieError;

# Defines the possible client error types
public type ClientError ResiliencyError|ClientAuthError|OutboundRequestError|
                            InboundResponseError|UnsupportedActionError|Http2ClientError|
                            MaximumWaitTimeExceededError|SslError|GenericClientError|CookieHandlingError|
                            RemoteServerError|ClientRequestError|IllegalDataBindingStateError;

# Defines the possible listener error types
public type ListenerError GenericListenerError|InboundRequestError|OutboundResponseError;

function getGenericClientError(string message, error cause) returns GenericClientError {
    GenericClientError err = error(GENERIC_CLIENT_ERROR, message = message, cause = cause);
    return err;
}
