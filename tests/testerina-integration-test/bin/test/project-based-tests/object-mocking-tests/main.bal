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

PersonObj pObj = new ("John", "Doe");

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

public function getAttribute() returns TestHttpClient:AttributeDAO|error {
    stream<record {}, error?> attributeResult = clientEndpoint->get_stream("path");
    stream<TestHttpClient:AttributeDAO, error> attributeStream = <stream<TestHttpClient:AttributeDAO, error>>attributeResult;
    record {|TestHttpClient:AttributeDAO value;|}|error attribute = attributeStream.next();
    error? closeErr = attributeStream.close();
    if (attribute is record {|TestHttpClient:AttributeDAO value;|}) {
        return <TestHttpClient:AttributeDAO>attribute.value;
    } else {
        return attribute;
    }
}

function getPerson(string id, typedesc<int|string> td) returns int|string {
    return pObj.getValue(id, td);
}

function getTheError() returns string|error {
    return clientEndpoint->getError("/path1");
}

public class PersonObj {
    string fname;
    string lname;

    public function init(string fname, string lname) {
        self.fname = fname;
        self.lname = lname;
    }

    function name() returns string => self.fname + " " + self.lname;

    public function getValue(string id, typedesc<int|string> td) returns td = @java:Method {
        'class: "org.ballerinalang.testerina.utils.ObjectMockTestInterop"
    } external;
}
