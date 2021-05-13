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

import object_mocking.TestHttpClient;
import ballerina/jballerina.java;

TestHttpClient:HttpClient clientEndpoint = new ("http://realurl.com");

function doGet() returns string {
    string result = clientEndpoint->get("/path1");
    return result;
}

function doGetRepeat() returns string {
    string result = clientEndpoint->get("/path2");

    if (result == "response1") {
        result = clientEndpoint->get("/path1");
    }

    return result;
}

function getClientUrl() returns string {
    return clientEndpoint.url;
}

public class PersonObj {
    string fname;
    string lname;

    public function init(string fname, string lname) {
        self.fname = fname;
        self.lname = lname;
    }

    function name() returns string => self.fname + " " + self.lname;

    public function getObjectValue(typedesc<int|float|decimal|string|boolean> td) returns td = @java:Method {
        'class: "org.ballerinalang.testerina.utils.VariableReturnType"
    } external;
}
