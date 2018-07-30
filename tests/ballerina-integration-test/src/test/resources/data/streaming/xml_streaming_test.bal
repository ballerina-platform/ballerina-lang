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
import ballerina/io;
import ballerina/h2;

endpoint h2:Client testDB {
    path: "../../tempdb/",
    name: "STREAMING_XML_TEST_DB",
    username: "SA",
    password: "",
    poolOptions: { maximumPoolSize: 1 },
    dbOptions: { IFEXISTS: true }
};

service<http:Service> dataService bind { port: 9090 } {

    getData(endpoint caller, http:Request req) {
        http:Response res = new;

        var selectRet = testDB->select("SELECT * FROM Data", ());
        table dt;
        match selectRet {
            table tableReturned => dt = tableReturned;
            error e => io:println("Select data from Data table failed: " + e.message);
        }

        xml xmlConversionRet = check <xml>dt;
        res.setPayload(untaint xmlConversionRet);

        caller->respond(res) but {
            error e => io:println("Error sending response")
        };
    }
}
