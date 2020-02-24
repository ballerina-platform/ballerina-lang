// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/io;
import ballerina/mime;

# Represents HTTP/1.0 protocol
const string HTTP_1_0 = "1.0";

# Represents HTTP/1.1 protocol
const string HTTP_1_1 = "1.1";

# Represents HTTP/2.0 protocol
const string HTTP_2_0 = "2.0";

# Defines the supported HTTP protocols.
#
# `HTTP_1_0`: HTTP/1.0 protocol
# `HTTP_1_1`: HTTP/1.1 protocol
# `HTTP_2_0`: HTTP/2.0 protocol
public type HttpVersion HTTP_1_0|HTTP_1_1|HTTP_2_0;

# Represents http protocol scheme
const string HTTP_SCHEME = "http://";

# Represents https protocol scheme
const string HTTPS_SCHEME = "https://";

# Constant for the http error code
public const string HTTP_ERROR_CODE = "{ballerina/http}HTTPError";

# Constant for the default listener endpoint timeout
const int DEFAULT_LISTENER_TIMEOUT = 120000; //2 mins

# Constant for the default failover starting index for failover endpoints
const int DEFAULT_FAILOVER_EP_STARTING_INDEX = 0;

# Maximum number of requests that can be processed at a given time on a single connection.
const int MAX_PIPELINED_REQUESTS = 10;

# Represents multipart primary type
public const string MULTIPART_AS_PRIMARY_TYPE = "multipart/";

# Constant for the HTTP FORWARD method
public const HTTP_FORWARD = "FORWARD";

# Constant for the HTTP GET method
public const HTTP_GET = "GET";

# Constant for the HTTP POST method
public const HTTP_POST = "POST";

# Constant for the HTTP DELETE method
public const HTTP_DELETE = "DELETE";

# Constant for the HTTP OPTIONS method
public const HTTP_OPTIONS = "OPTIONS";

# Constant for the HTTP PUT method
public const HTTP_PUT = "PUT";

# Constant for the HTTP PATCH method
public const HTTP_PATCH = "PATCH";

# Constant for the HTTP HEAD method
public const HTTP_HEAD = "HEAD";

# constant for the HTTP SUBMIT method
public const HTTP_SUBMIT = "SUBMIT";

# Constant for the identify not an HTTP Operation
public const HTTP_NONE = "NONE";

# Defines the possible values for the chunking configuration in HTTP services and clients.
#
# `AUTO`: If the payload is less than 8KB, content-length header is set in the outbound request/response,
#         otherwise chunking header is set in the outbound request/response
# `ALWAYS`: Always set chunking header in the response
# `NEVER`: Never set the chunking header even if the payload is larger than 8KB in the outbound request/response
public type Chunking CHUNKING_AUTO|CHUNKING_ALWAYS|CHUNKING_NEVER;

# If the payload is less than 8KB, content-length header is set in the outbound request/response,
# otherwise chunking header is set in the outbound request/response.}
public const CHUNKING_AUTO = "AUTO";

# Always set chunking header in the response.
public const CHUNKING_ALWAYS = "ALWAYS";

# Never set the chunking header even if the payload is larger than 8KB in the outbound request/response.
public const CHUNKING_NEVER = "NEVER";

# Options to compress using gzip or deflate.
#
# `AUTO`: When service behaves as a HTTP gateway inbound request/response accept-encoding option is set as the
#         outbound request/response accept-encoding/content-encoding option
# `ALWAYS`: Always set accept-encoding/content-encoding in outbound request/response
# `NEVER`: Never set accept-encoding/content-encoding header in outbound request/response
public type Compression COMPRESSION_AUTO|COMPRESSION_ALWAYS|COMPRESSION_NEVER;

# When service behaves as a HTTP gateway inbound request/response accept-encoding option is set as the
# outbound request/response accept-encoding/content-encoding option.
public const COMPRESSION_AUTO = "AUTO";

# Always set accept-encoding/content-encoding in outbound request/response.
public const COMPRESSION_ALWAYS = "ALWAYS";

# Never set accept-encoding/content-encoding header in outbound request/response.
public const COMPRESSION_NEVER = "NEVER";

# The types of messages that are accepted by HTTP `client` when sending out the outbound request.
public type RequestMessage Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|();

# The types of messages that are accepted by HTTP `listener` when sending out the outbound response.
public type ResponseMessage Response|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|();

# The type of user defined custom record.
public type CustomRecordType record {| anydata...; |};

# The types of response payload that are returned by HTTP `client` after data binding operation.
public type PayloadType string|xml|json|byte[]|CustomRecordType|CustomRecordType[];

# The types of data values that are expected by HTTP `client` to return after data binding operation.
public type TargetType typedesc<Response|string|xml|json|byte[]|CustomRecordType| CustomRecordType[]>;

# Defines the HTTP operations related to circuit breaker, failover and load balancer.Read
#
# `FORWARD`: Forward the specified payload
# `GET`: Request a resource
# `POST`: Create a new resource
# `DELETE`: Deletes the specified resource
# `OPTIONS`: Request communication options available
# `PUT`: Replace the target resource
# `PATCH`: Apply partial modification to the resource
# `HEAD`: Identical to `GET` but no resource body should be returned
# `SUBMIT`: Submits a http request and returns an HttpFuture object
# `NONE`: No operation should be performed
public type HttpOperation HTTP_FORWARD|HTTP_GET|HTTP_POST|HTTP_DELETE|HTTP_OPTIONS|HTTP_PUT|HTTP_PATCH|HTTP_HEAD
                                                                                                |HTTP_SUBMIT|HTTP_NONE;

// Common type used for HttpFuture and Response used for resiliency clients.
type HttpResponse Response|HttpFuture;

# A record for configuring SSL/TLS protocol and version to be used.
#
# + name - SSL Protocol to be used (e.g.: TLS1.2)
# + versions - SSL/TLS protocols to be enabled (e.g.: TLSv1,TLSv1.1,TLSv1.2)
public type Protocols record {|
    string name = "";
    string[] versions = [];
|};

# A record for providing configurations for certificate revocation status checks.
#
# + enable - The status of `validateCertEnabled`
# + cacheSize - Maximum size of the cache
# + cacheValidityPeriod - The time period for which a cache entry is valid
public type ValidateCert record {|
    boolean enable = false;
    int cacheSize = 0;
    int cacheValidityPeriod = 0;
|};

# A record for providing configurations for certificate revocation status checks.
#
# + enable - The status of OCSP stapling
# + cacheSize - Maximum size of the cache
# + cacheValidityPeriod - The time period for which a cache entry is valid
public type ListenerOcspStapling record {|
    boolean enable = false;
    int cacheSize = 0;
    int cacheValidityPeriod = 0;
|};

# A record for providing configurations for content compression.
#
# + enable - The status of compression
# + contentTypes - Content types which are allowed for compression
public type CompressionConfig record {|
    Compression enable = COMPRESSION_AUTO;
    string[] contentTypes = [];
|};

type HTTPError record {
    string message = "";
};
