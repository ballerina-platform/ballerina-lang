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

// Ballerina HTTP Client Error Types

// Resiliency errors
# Represents a client error that occurred due to all the failover endpoint failure
public type FailoverAllEndpointsFailedError distinct error;

# Represents a client error that occurred due to failover action failure
public type FailoverActionFailedError distinct error;

# Represents a client error that occurred due to upstream service unavailability
public type UpstreamServiceUnavailableError distinct error;

# Represents a client error that occurred due to all the load balance endpoint failure
public type AllLoadBalanceEndpointsFailedError distinct error;

# Represents a client error that occurred due to circuit breaker configuration error.
public type CircuitBreakerConfigError distinct error;

# Represents a client error that occurred due to all the the retry attempts failure
public type AllRetryAttemptsFailed distinct error;

# Represents the error that triggered upon a request/response idle timeout
public type IdleTimeoutError distinct error;

// Security errors
# Represents a listener error that occurred due to inbound request authentication failure
public type AuthenticationError distinct error;

# Represents a listener error that occurred due to inbound request authorization failure
public type AuthorizationError distinct error;

// Outbound request errors in client
# Represents a client error that occurred due to outbound request initialization failure
public type InitializingOutboundRequestError distinct error;

# Represents a client error that occurred while writing outbound request headers
public type WritingOutboundRequestHeadersError distinct error;

# Represents a client error that occurred while writing outbound request entity body
public type WritingOutboundRequestBodyError distinct error;

// Inbound response errors in client
# Represents a client error that occurred due to inbound response initialization failure
public type InitializingInboundResponseError distinct error;

# Represents a client error that occurred while reading inbound response headers
public type ReadingInboundResponseHeadersError distinct error;

# Represents a client error that occurred while reading inbound response entity body
public type ReadingInboundResponseBodyError distinct error;

//Inbound request errors in listener
# Represents a listener error that occurred due to inbound request initialization failure
public type InitializingInboundRequestError distinct error;

# Represents a listener error that occurred while reading inbound request headers
public type ReadingInboundRequestHeadersError distinct error;

# Represents a listener error that occurred while writing the inbound request entity body
public type ReadingInboundRequestBodyError distinct error;

// Outbound response errors in listener
# Represents a listener error that occurred due to outbound response initialization failure
public type InitializingOutboundResponseError distinct error;

# Represents a listener error that occurred while writing outbound response headers
public type WritingOutboundResponseHeadersError distinct error;

# Represents a listener error that occurred while writing outbound response entity body
public type WritingOutboundResponseBodyError distinct error;

# Represents an error that occurred due to 100 continue response initialization failure
public type Initiating100ContinueResponseError distinct error;

# Represents an error that occurred while writing 100 continue response
public type Writing100ContinueResponseError distinct error;

# Represents a cookie error that occurred when sending cookies in the response
public type InvalidCookieError distinct error;

// Generic errors (mostly to wrap errors from other modules)
# Represents a generic client error
public type GenericClientError distinct error;

# Represents a generic listener error
public type GenericListenerError distinct error;

// Other client-related errors
# Represents a client error that occurred due to unsupported action invocation
public type UnsupportedActionError distinct error;

# Represents an HTTP/2 client generic error
public type Http2ClientError distinct error;

# Represents a client error that occurred exceeding maximum wait time
public type MaximumWaitTimeExceededError distinct error;

# Represents a client error that occurred due to SSL failure
public type SslError distinct error;

# Represents a cookie error that occurred when using the cookies
public type CookieHandlingError distinct error;

// Ballerina HTTP Union Errors
# Defines the resiliency error types that returned from client
public type ResiliencyError FailoverAllEndpointsFailedError|FailoverActionFailedError|
                            UpstreamServiceUnavailableError|AllLoadBalanceEndpointsFailedError|
                            AllRetryAttemptsFailed|IdleTimeoutError;

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
                            MaximumWaitTimeExceededError|SslError|GenericClientError|CookieHandlingError;

# Defines the possible listener error types
public type ListenerError GenericListenerError|InboundRequestError|OutboundResponseError;
