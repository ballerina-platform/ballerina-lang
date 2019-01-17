// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

# TrustStore record represents trust store related options to be used for HTTP client/service invocation.
#
# + path - File path to trust store file
# + password - Trust store password
public type TrustStore record {
    string path = "";
    string password = "";
    !...;
};

# KeyStore record represents key store related options to be used for HTTP client/service invocation.
#
# + path - File path to key store file
# + password - Key store password
public type KeyStore record {
    string path = "";
    string password = "";
    !...;
};

# Protocols record represents SSL/TLS protocol related options to be used for HTTP client/service invocation.
#
# + name - SSL Protocol to be used. eg TLS1.2
# + versions - SSL/TLS protocols to be enabled. eg TLSv1,TLSv1.1,TLSv1.2
public type Protocols record {
    string name = "";
    string[] versions = [];
    !...;
};

# ValidateCert record represents options related to check whether a certificate is revoked or not.
#
# + enable - The status of validateCertEnabled
# + cacheSize - Maximum size of the cache
# + cacheValidityPeriod - Time duration of cache validity period
public type ValidateCert record {
    boolean enable = false;
    int cacheSize = 0;
    int cacheValidityPeriod = 0;
    !...;
};

# OcspStapling record represents options related to check whether a certificate is revoked or not.
#
# + enable - The status of OcspStapling
# + cacheSize - Maximum size of the cache
# + cacheValidityPeriod - Time duration of cache validity period
public type ServiceOcspStapling record {
    boolean enable = false;
    int cacheSize = 0;
    int cacheValidityPeriod = 0;
    !...;
};

# Defines the possible values for the keep-alive configuration in service and client endpoints.
public type KeepAlive KEEPALIVE_AUTO|KEEPALIVE_ALWAYS|KEEPALIVE_NEVER;

# Decides to keep the connection alive or not based on the `connection` header of the client request }
public const KEEPALIVE_AUTO = "AUTO";
# Keeps the connection alive irrespective of the `connection` header value }
public const KEEPALIVE_ALWAYS = "ALWAYS";
# Closes the connection irrespective of the `connection` header value }
public const KEEPALIVE_NEVER = "NEVER";

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

# Defines the possible values for the chunking configuration in HTTP services and clients.
#
# `AUTO`: If the payload is less than 8KB, content-length header is set in the outbound request/response,
#         otherwise chunking header is set in the outbound request/response
# `ALWAYS`: Always set chunking header in the response
# `NEVER`: Never set the chunking header even if the payload is larger than 8KB in the outbound request/response
public type Chunking CHUNKING_AUTO|CHUNKING_ALWAYS|CHUNKING_NEVER;

# If the payload is less than 8KB, content-length header is set in the outbound request/response,
# otherwise chunking header is set in the outbound request/response.
public const CHUNKING_AUTO = "AUTO";

# Always set chunking header in the response.
public const CHUNKING_ALWAYS = "ALWAYS";

# Never set the chunking header even if the payload is larger than 8KB in the outbound request/response.
public const CHUNKING_NEVER = "NEVER";
