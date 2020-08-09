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

# Constant for telemetry tag http.url
const HTTP_URL = "http.url";

# Constant for telemetry tag http.method
const HTTP_METHOD = "http.method";

# Constant for telemetry tag http.status_code_group
const HTTP_STATUS_CODE_GROUP = "http.status_code_group";

# Constant for telemetry tag http.base_url
const HTTP_BASE_URL = "http.base_url";

# Constant for status code range suffix
const STATUS_CODE_GROUP_SUFFIX = "xx";

# Defines the position of the headers in the request/response.
#
# `LEADING`: Header is placed before the payload of the request/response
# `TRAILING`: Header is placed after the payload of the request/response
public type HeaderPosition LEADING|TRAILING;

# Header is placed before the payload of the request/response.
public const LEADING = "leading";

# Header is placed after the payload of the request/response.
public const TRAILING = "trailing";
