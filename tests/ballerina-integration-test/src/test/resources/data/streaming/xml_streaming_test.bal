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
import ballerina/log;

h2:Client testDB = new({
    path: "../../tempdb/",
    name: "STREAMING_XML_TEST_DB",
    username: "SA",
    password: "",
    poolOptions: { maximumPoolSize: 1 },
    dbOptions: { IFEXISTS: true }
});

listener http:Listener dataServiceListener = new(9090);

service dataService on dataServiceListener {

    resource function getData(http:Caller caller, http:Request req) {

        var selectRet = testDB->select("SELECT * FROM Data", ());
        if (selectRet is table<record {}>) {
            var xmlConversionRet = xml.convert(selectRet);
            if (xmlConversionRet is xml) {
                var responseToCaller = caller->respond(untaint xmlConversionRet);
                if (responseToCaller is error) {
                    log:printError("Error sending response", err = responseToCaller);
                }
            } else {
                panic xmlConversionRet;
            }
        } else {
            log:printError("Select data from Data table failed: " + selectRet.reason());
        }
    }
}
