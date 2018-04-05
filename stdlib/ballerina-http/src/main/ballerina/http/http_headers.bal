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


@final // Please maintain alphabetical order when adding new header names
documentation { HTTP header key 'age'. Gives the current age of a cached HTTP response. }
public string AGE = "age";

@final 
documentation { HTTP header key 'authorization' }
public string AUTHORIZATION = "authorization";

@final 
documentation { HTTP header key 'cache-control'. Specifies the cache control directives required for the function of HTTP caches. }
public string CACHE_CONTROL = "cache-control";

@final 
documentation { HTTP header key 'content-length'. Specifies the size of the response body in bytes. }
public string CONTENT_LENGTH = "content-length";

@final 
documentation { HTTP header key 'date'. The timestamp at the time the response was generated/received. }
public string DATE = "date";

@final 
documentation { HTTP header key 'etag'. A finger print for a resource which is used by HTTP caches to identify whether a resource representation has changed. }
public string ETAG = "etag";

@final 
documentation { HTTP header key 'expect'. Specifies expectations to be fulfilled by the server. }
public string EXPECT = "expect";

@final 
documentation { HTTP header key 'expires'. Specifies the time at which the response becomes stale. }
public string EXPIRES = "expires";

@final 
documentation { HTTP header key 'if-match' }
public string IF_MATCH = "if-match";

@final 
documentation { HTTP header key 'if-modified-since'. Used when validating (with the origin server) whether a cached response is still valid. If the representation of the resource has modified since the timestamp in this field, a 304 response is returned. }
public string IF_MODIFIED_SINCE = "if-modified-since";

@final 
documentation { HTTP header key 'if-none-match'. Used when validating (with the origin server) whether a cached response is still valid. If the ETag provided in this field matches the representation of the requested resource, a 304 response is returned. }
public string IF_NONE_MATCH = "if-none-match";

@final 
documentation { HTTP header key 'if-range' }
public string IF_RANGE = "if-range";

@final 
documentation { HTTP header key 'if-unmodified-since' }
public string IF_UNMODIFIED_SINCE = "if-unmodified-since";

@final 
documentation { HTTP header key 'last-modified'. The time at which the resource was last modified. }
public string LAST_MODIFIED = "last-modified";

@final 
documentation { HTTP header key 'location'. Indicates the URL to redirect a request to. }
public string LOCATION = "location";

@final 
documentation { HTTP header key 'pragma'. Used in dealing with HTTP 1.0 caches which do not understand the 'cache-control' header. }
public string PRAGMA = "pragma";

@final 
documentation { HTTP header key 'warning'. Specifies warnings generated when serving stale responses from HTTP caches. }
public string WARNING = "warning";
