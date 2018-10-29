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

import ballerina/http;
import ballerina/h2;
import ballerina/sql;

endpoint http:Listener testEp {
    port:9090
};

endpoint h2:Client testDB {
    path: "./target/tempdb/",
    name: "TEST_DB",
    username: "SA",
    password: "",
    poolOptions: { maximumPoolSize: 10 }
};

@http:ServiceConfig {
    basePath:"/test"
}
service<http:Service> metricsTest bind testEp {
    @http:ResourceConfig {
        path: "/"
    }
    getProduct (endpoint caller, http:Request req) {
        var dbResult = testDB -> select("SELECT * FROM Products", null);
        table dbTable = check dbResult;
        json jData = check <json>dbTable;
        string result = jData.toString();
        http:Response resp = new;
        resp.setTextPayload(untaint result);
        _ = caller -> respond(resp);
    }
}
