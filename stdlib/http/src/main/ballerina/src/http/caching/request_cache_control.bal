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

# Configures the cache control directives for an `http:Request`.
#
# + noCache - Sets the `no-cache` directive
# + noStore - Sets the `no-store` directive
# + noTransform - Sets the `no-transform` directive
# + onlyIfCached - Sets the `only-if-cached` directive
# + maxAge - Sets the `max-age` directive
# + maxStale - Sets the `max-stale` directive
# + minFresh - Sets the `min-fresh` directive
public class RequestCacheControl {

    public boolean noCache = false;
    public boolean noStore = false;
    public boolean noTransform = false;
    public boolean onlyIfCached = false;
    public int maxAge = -1;
    public int maxStale = -1;
    public int minFresh = -1;

    # Builds the cache control directives string from the current `http:RequestCacheControl` configurations.
    #
    # + return - The cache control directives string to be used in the `cache-control` header
    public function buildCacheControlDirectives () returns string {
        string[] directives = [];
        int i = 0;

        if (self.noCache) {
            directives[i] = NO_CACHE;
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

        if (self.onlyIfCached) {
            directives[i] = ONLY_IF_CACHED;
            i = i + 1;
        }

        if (self.maxAge >= 0) {
            directives[i] = MAX_AGE + "=" + self.maxAge.toString();
            i = i + 1;
        }

        if (self.maxStale == MAX_STALE_ANY_AGE) {
            directives[i] = MAX_STALE;
            i = i + 1;
        } else if (self.maxStale >= 0) {
            directives[i] = MAX_STALE + "=" + self.maxStale.toString();
            i = i + 1;
        }

        if (self.minFresh >= 0) {
            directives[i] = MIN_FRESH + "=" + self.minFresh.toString();
            i = i + 1;
        }

        return buildCommaSeparatedString(directives);
    }
}

function setRequestCacheControlHeader(Request request) {
    var requestCacheControl = request.cacheControl;
    if (requestCacheControl is RequestCacheControl) {
        if (!request.hasHeader(CACHE_CONTROL)) {
            request.setHeader(CACHE_CONTROL, requestCacheControl.buildCacheControlDirectives());
        }
    }
}
