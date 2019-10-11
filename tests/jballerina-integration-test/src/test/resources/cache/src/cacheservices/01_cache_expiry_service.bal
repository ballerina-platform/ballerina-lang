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

import ballerina/cache;
import ballerina/http;

listener http:Listener httpListener = new(19001);

cache:Cache cache = new;

public function main() {
    cache = new(10000, 10, 0.1);
    cache.put("k1", "v1");
    cache.put("k2", "v2");
    cache.put("k3", "v3");
    cache.put("k4", "v4");
}

function getCacheSize() returns (int) {
    return cache.size();
}

@http:ServiceConfig {
    basePath: "/"
}
service CacheValueService on httpListener {
    @http:ResourceConfig {
        methods: ["GET"]
    }
    resource function getCacheSize(http:Caller caller, http:Request request) {
        var result = caller->respond(getCacheSize());
    }
}
