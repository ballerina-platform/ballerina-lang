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

package ballerina.http;

// Please maintain alphabetical order when adding new header names
@Description {value:"HTTP header key 'age'. Gives the current age of a cached HTTP response."}
@final public string AGE = "age";
@Description {value:"HTTP header key 'authorization'"}
@final public string AUTHORIZATION = "authorization";
@Description {value:"HTTP header key 'cache-control'. Specifies the cache control directives required for the function of HTTP caches."}
@final public string CACHE_CONTROL = "cache-control";
@Description {value:"HTTP header key 'date'. The timestamp at the time the response was generated/received."}
@final public string DATE = "date";
@Description {value:"HTTP header key 'etag'. A finger print for a resource which is used by HTTP caches to identify whether a resource representation has changed."}
@final public string ETAG = "etag";
@Description {value:"HTTP header key 'expires'. Specifies the time at which the response becomes stale."}
@final public string EXPIRES = "expires";
@Description {value:"HTTP header key 'if-match'"}
@final public string IF_MATCH = "if-match";
@Description {value:"HTTP header key 'if-modified-since'. Used when validating (with the origin server) whether a cached response is still valid. If the representation of the resource has modified since the timestamp in this field, a 304 response is returned."}
@final public string IF_MODIFIED_SINCE = "if-modified-since";
@Description {value:"HTTP header key 'if-none-match'. Used when validating (with the origin server) whether a cached response is still valid. If the ETag provided in this field matches the representation of the requested resource, a 304 response is returned."}
@final public string IF_NONE_MATCH = "if-none-match";
@Description {value:"HTTP header key 'if-range'"}
@final public string IF_RANGE = "if-range";
@Description {value:"HTTP header key 'if-unmodified-since'"}
@final public string IF_UNMODIFIED_SINCE = "if-unmodified-since";
@Description {value:"HTTP header key 'last-modified'. The time at which the resource was last modified."}
@final public string LAST_MODIFIED = "last-modified";
@Description {value:"HTTP header key 'pragma'"}
@final public string PRAGMA = "pragma";
@Description {value:"HTTP header key 'warning'. Specifies warnings generated when serving stale responses from HTTP caches."}
@final public string WARNING = "warning";
