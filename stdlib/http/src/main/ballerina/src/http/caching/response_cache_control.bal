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

# Configures cache control directives for an `http:Response`.
#
# + mustRevalidate - Sets the `must-revalidate` directive
# + noCache - Sets the `no-cache` directive
# + noStore - Sets the `no-store` directive
# + noTransform - Sets the `no-transform` directive
# + isPrivate - Sets the `private` and `public` directives
# + proxyRevalidate - Sets the `proxy-revalidate` directive
# + maxAge - Sets the `max-age` directive
# + sMaxAge - Sets the `s-maxage` directive
# + noCacheFields - Optional fields for the `no-cache` directive. Before sending a listed field in a response, it
#                   must be validated with the origin server.
# + privateFields - Optional fields for the `private` directive. A cache can omit the fields specified and store
#                   the rest of the response.
public class ResponseCacheControl {

    public boolean mustRevalidate = false;
    public boolean noCache = false;
    public boolean noStore = false;
    public boolean noTransform = false;
    public boolean isPrivate = false;
    public boolean proxyRevalidate = false;
    public int maxAge = -1;
    public int sMaxAge = -1;
    public string[] noCacheFields = [];
    public string[] privateFields = [];

    # Builds the cache control directives string from the current `http:ResponseCacheControl` configurations.
    #
    # + return - The cache control directives string to be used in the `cache-control` header
    public function buildCacheControlDirectives () returns string {
        string[] directives = [];
        int i = 0;

        if (self.mustRevalidate) {
            directives[i] = MUST_REVALIDATE;
            i = i + 1;
        }

        if (self.noCache) {
            directives[i] = NO_CACHE + appendFields(self.noCacheFields);
            i = i + 1;
        }

        if (self.noStore) {
            directives[i] = NO_STORE;
            i = i + 1;
        }

        if (self.noTransform) {
            directives[i] = NO_TRANSFORM;
            i = i + 1;
        }

        if (self.isPrivate) {
            directives[i] = PRIVATE + appendFields(self.privateFields);
        } else {
            directives[i] = PUBLIC;
        }
        i = i + 1;

        if (self.proxyRevalidate) {
            directives[i] = PROXY_REVALIDATE;
            i = i + 1;
        }

        if (self.maxAge >= 0) {
            directives[i] = MAX_AGE + "=" + self.maxAge.toString();
            i = i + 1;
        }

        if (self.sMaxAge >= 0) {
            directives[i] = S_MAX_AGE + "=" + self.sMaxAge.toString();
            i = i + 1;
        }

        return buildCommaSeparatedString(directives);
    }
}

function setResponseCacheControlHeader(Response response) {
    var responseCacheControl = response.cacheControl;
    if (responseCacheControl is ResponseCacheControl) {
        if (!response.hasHeader(CACHE_CONTROL)) {
            response.setHeader(CACHE_CONTROL, responseCacheControl.buildCacheControlDirectives());
        }
    }
}
