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

import ballerina/sql;
import ballerina/h2;
import ballerina/io;
import ballerina/http;

listener http:MockListener testEP = new(9090);

h2:Client testDB = new({
        path: "./target/tempdb/",
        name: "TEST_DATA_TABLE_H2",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    });

@http:ServiceConfig { 
    basePath: "/foo" 
}
service MyService on testEP {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/bar1"
    }
    resource function myResource1 (http:Caller caller, http:Request req) {
        var selectRet = testDB->select("SELECT int_type, long_type, float_type, double_type,
                  boolean_type, string_type from DataTable WHERE row_id = 1", ());
        json result = {};
        if (selectRet is table<record {}>) {
            var ret = json.convert(selectRet);
            if (ret is json) {
                result = ret;
            } else {
                result = { Error: ret.reason() };
            }
        } else {
            result = { Error: selectRet.reason() };
        }

        http:Response res = new;
        res.setPayload(untaint result);
        var respondResult = caller->respond(res);
        if (respondResult is error) {
            io:println("Error sending response");
        }
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/bar2"
    }
    resource function myResource2 (http:Caller caller, http:Request req) {

        var selectRet = testDB->select("SELECT int_type, long_type, float_type, double_type,
                  boolean_type, string_type from DataTable WHERE row_id = 1", ());
        json result = {};
        string statusVal = "ERROR";
        if (selectRet is table<record {}>) {
            var ret = json.convert(selectRet);
            if (ret is json) {
                result = ret;
                statusVal = "SUCCESS";
            } else {
                result = { Error: ret.reason() };
            }
        } else {
            result = { Error: selectRet.reason() };
        }
        json j = { status: statusVal, resp: { value: result } };

        http:Response res = new;
        res.setPayload(untaint j);
        var respondResult = caller->respond(res);
        if (respondResult is error) {
            io:println("Error sending response");
        }
    }
}

function closeConnectionPool() {
    testDB.stop();
}
