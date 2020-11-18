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

import ballerina/java;

// Cache-control directives
# Forces the cache to validate a cached response with the origin server before serving.
public const string NO_CACHE = "no-cache";

# Instructs the cache to not store a response in non-volatile storage.
public const string NO_STORE = "no-store";

# Instructs intermediaries not to transform the payload.
public const string NO_TRANSFORM = "no-transform";

# When used in requests, `max-age` implies that clients are not willing to accept responses whose age is greater
# than `max-age`. When used in responses, the response is to be considered stale after the specified
# number of seconds.
public const string MAX_AGE = "max-age";


// Request only cache-control directives
# Indicates that the client is willing to accept responses which have exceeded their freshness lifetime by no more
# than the specified number of seconds.
public const string MAX_STALE = "max-stale";

# Indicates that the client is only accepting responses whose freshness lifetime >= current age + min-fresh.
public const string MIN_FRESH = "min-fresh";

# Indicates that the client is only willing to accept a cached response. A cached response is served subject to
# other constraints posed by the request.
public const string ONLY_IF_CACHED = "only-if-cached";


// Response only cache-control directives
# Indicates that once the response has become stale, it should not be reused for subsequent requests without
# validating with the origin server.
public const string MUST_REVALIDATE = "must-revalidate";

# Indicates that any cache may store the response.
public const string PUBLIC = "public";

# Indicates that the response is intended for a single user and should not be stored by shared caches.
public const string PRIVATE = "private";

# Has the same semantics as `must-revalidate`, except that this does not apply to private caches.
public const string PROXY_REVALIDATE = "proxy-revalidate";

# In shared caches, `s-maxage` overrides the `max-age` or `expires` header field.
public const string S_MAX_AGE = "s-maxage";


// Other constants
# Setting this as the `max-stale` directives indicates that the `max-stale` directive does not specify a limit.
public const int MAX_STALE_ANY_AGE = 9223372036854775807;


// Non-public constants/final vars
final string WARNING_AGENT = getWarningAgent();

final string WARNING_110_RESPONSE_IS_STALE = "110 " + WARNING_AGENT + " \"Response is Stale\"";
final string WARNING_111_REVALIDATION_FAILED = "111 " + WARNING_AGENT + " \"Revalidation Failed\"";

const string WEAK_VALIDATOR_TAG = "W/";
const int STALE = 0;

function getWarningAgent() returns string {
    string ballerinaVersion = getProperty("ballerina.version");
    return "ballerina-http-caching-client/" + ballerinaVersion;
}

function getProperty(@untainted string name) returns string = @java:Method {
    name: "getProperty",
    'class: "org.ballerinalang.net.http.util.CacheUtils"
} external;
