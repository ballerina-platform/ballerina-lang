// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import ballerina/log;
import ballerinax/java.jdbc;

jdbc:Client testDB = new({
        url: "jdbc:h2:file:../../tempdb/STREAMING_TEST_DB",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 },
        dbOptions: { IFEXISTS: true }
    });

listener http:Listener dataServiceListener = new(9090);

service dataService on dataServiceListener {

    resource function getXmlData(http:Caller caller, http:Request req) {

        var selectRet = testDB->select("SELECT * FROM Data", ());
        if (selectRet is table<record {}>) {
            var xmlConversionRet = typedesc<xml>.constructFrom(selectRet);
            if (xmlConversionRet is xml) {
                var responseToCaller = caller->respond(<@untainted> xmlConversionRet);
                if (responseToCaller is error) {
                    log:printError("Error sending response", responseToCaller);
                }
            } else {
                panic xmlConversionRet;
            }
        } else {
            error e = selectRet;
            log:printError("Select data from Data table failed: " + e.reason());
        }
    }

    resource function getJsonData(http:Caller caller, http:Request req) {

        var selectRet = testDB->select("SELECT * FROM Data", ());
        if (selectRet is table<record {}>) {
            var jsonConversionRet = typedesc<json>.constructFrom(selectRet);
            if (jsonConversionRet is json) {
                var responseToCaller = caller->respond(<@untainted> jsonConversionRet);
                if (responseToCaller is error) {
                    log:printError("Error sending response", responseToCaller);
                }
            } else {
                panic jsonConversionRet;
            }
        } else {
            error e = selectRet;
            log:printError("Select data from Data table failed: " + e.reason());
        }
    }

    resource function getJsonDataAppended(http:Caller caller, http:Request req) {

        var selectRet = testDB->select("SELECT * FROM Data", ());
        if (selectRet is table<record {}>) {
            var jsonConversionRet = typedesc<json>.constructFrom(selectRet);
            if (jsonConversionRet is json) {
                json j = { status: "200", resp: { value: jsonConversionRet } };
                var responseToCaller = caller->respond(<@untainted> j);
                if (responseToCaller is error) {
                    log:printError("Error sending response", responseToCaller);
                }
            } else {
                panic jsonConversionRet;
            }
        } else {
            error e = selectRet;
            log:printError("Select data from Data table failed: " + e.reason());
        }
    }
}
