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
import ballerina/grpc;
import ballerina/log;

endpoint grpc:Listener ep98 {
    host:"localhost",
    port:9098
};

service HelloWorld98 bind ep98 {
    hello(endpoint caller, string name) {
        log:printInfo("name: " + name);
        string message = "Hello " + name;
        error? err;
        if (name == "invalid") {
            err = caller->sendError(grpc:ABORTED, "Operation aborted");
        } else {
            err = caller->send(message);
        }
        if (err is error) {
            log:printError(err.reason(), err = err);
        }
        _ = caller->complete();
    }

    testInt(endpoint caller, string age) {
        log:printInfo("age: " + age);
        int displayAge;
        if (age == "") {
            displayAge = -1;
        } else {
            displayAge = 1;
        }
        error? err = caller->send(displayAge);
        if (err is error) {
            log:printError(err.reason(), err = err);
        } else {
            log:printInfo("display age : " + displayAge);
        }
        _ = caller->complete();
    }

    testFloat(endpoint caller, float salary) {
        log:printInfo("gross salary: " + salary);
        string netSalary = <string>(salary * 0.88);
        error? err = caller->send(netSalary);
        if (err is error) {
            log:printError(err.reason(), err = err);
        } else {
            log:printInfo("net salary : " + netSalary);
        }
        _ = caller->complete();
    }
}
