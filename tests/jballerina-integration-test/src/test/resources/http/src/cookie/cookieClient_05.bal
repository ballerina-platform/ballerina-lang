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

import ballerina/file;
import ballerina/http;
import ballerina/io;

public function main() {
    http:CsvPersistentCookieHandler myPersistentStore = new("./cookie-test-data/client-5.csv");
    http:Client cookieClientEndpoint = new ("http://localhost:9253", {
            cookieConfig: { enabled: true, persistentCookieHandler: myPersistentStore }
        });
    worker w1 {
        http:Request req = new;
        var response = cookieClientEndpoint->get("/cookie/cookieBackend_1", req);
    }
    worker w2 {
        http:Request req = new;
        var response = cookieClientEndpoint->get("/cookie/cookieBackend_1", req);
    }
    worker w3 {
        http:Request req = new;
        var response = cookieClientEndpoint->get("/cookie/cookieBackend_1", req);
    }
    worker w4 {
        http:Request req = new;
        var response = cookieClientEndpoint->get("/cookie/cookieBackend_1", req);
    }
    _ = wait {w1, w2, w3, w4};
    http:CookieStore? myCookieStore = cookieClientEndpoint.getCookieStore();
    if (myCookieStore is http:CookieStore) {
        http:Cookie[] cookies = myCookieStore.getAllCookies();
        io:println(cookies.length());
        foreach var item in cookies {
            io:println(item.name);
        }
    }
    error? removeResults = file:remove("./cookie-test-data", true);
}
