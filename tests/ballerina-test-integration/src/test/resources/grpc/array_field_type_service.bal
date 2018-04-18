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
import ballerina/io;
import ballerina/grpc;

endpoint grpc:Service ep {
    host:"localhost",
    port:9090
};

@grpc:serviceConfig {generateClientConnector:false}
service<grpc:Listener> HelloWorld bind ep {

    testIntArrayInput (endpoint client, TestInt req) {
        io:println(req);
        int [] numbers = req.values;
        int result;
        foreach number in numbers {
            result = result + number;
        }
        io:println("Result: " + result);
        grpc:ConnectorError err = client -> send(result);
        if (err != ()) {
            io:println("Error at helloWorld : " + err.message);
        }
        _ = client -> complete();
    }

    testStringArrayInput (endpoint client, TestString req) {
        io:println(req);
        string[] values = req.values;
        string result;
        foreach value in values {
            result = result + "," + value;
        }
        io:println("Result: " + result);
        grpc:ConnectorError err = client -> send(result);
        if (err != ()) {
            io:println("Error at helloWorld : " + err.message);
        }
        _ = client -> complete();
    }

    testFloatArrayInput (endpoint client, TestFloat req) {
        io:println(req);
        float[] values = req.values;
        float result;
        foreach value in values {
            result = result + value;
        }
        io:println("Result: " + result);
        grpc:ConnectorError err = client -> send(result);
        if (err != ()) {
            io:println("Error at helloWorld : " + err.message);
        }
        _ = client -> complete();
    }

    testBooleanArrayInput (endpoint client, TestBoolean req) {
        io:println(req);
        boolean[] values = req.values;
        boolean result;
        foreach value in values {
            result = result || value;
        }
        io:println("Result: " + result);
        grpc:ConnectorError err = client -> send(result);
        if (err != ()) {
            io:println("Error at helloWorld : " + err.message);
        }
        _ = client -> complete();
    }

    testStructArrayInput (endpoint client, TestStruct req) {
        io:println(req);
        A[] values = req.values;
        string result;
        foreach value in values {
            result = result + "," + value.name;
        }
        grpc:ConnectorError err = client -> send(result);
        if (err != ()) {
            io:println("Error at helloWorld : " + err.message);
        }
        _ = client -> complete();
    }

    testIntArrayOutput (endpoint client) {
        TestInt intArray = {values:[1,2,3,4,5]};
        io:println("Response: ");
        io:println(intArray);
        grpc:ConnectorError err = client -> send(intArray);
        if (err != ()) {
            io:println("Error at helloWorld : " + err.message);
        }
        _ = client -> complete();
    }

    testStringArrayOutput (endpoint client) {
        TestString stringArray = {values:["A", "B", "C"]};
        io:println("Response: ");
        io:println(stringArray);
        grpc:ConnectorError err = client -> send(stringArray);
        if (err != ()) {
            io:println("Error at helloWorld : " + err.message);
        }
        _ = client -> complete();
    }

    testFloatArrayOutput (endpoint client) {
        TestFloat floatArray = {values:[1.1, 1.2, 1.3, 1.4, 1.5]};
        io:println("Response: ");
        io:println(floatArray);
        grpc:ConnectorError err = client -> send(floatArray);
        if (err != ()) {
            io:println("Error at helloWorld : " + err.message);
        }
        _ = client -> complete();
    }

    testBooleanArrayOutput (endpoint client) {
        TestBoolean booleanArray = {values:[true, false, true]};
        io:println("Response: ");
        io:println(booleanArray);
        grpc:ConnectorError err = client -> send(booleanArray);
        if (err != ()) {
            io:println("Error at helloWorld : " + err.message);
        }
        _ = client -> complete();
    }

    testStructArrayOutput (endpoint client) {
        A a1 = {name: "Sam"};
        A a2 = {name: "John"};
        TestStruct structArray = {values:[a1, a2]};
        grpc:ConnectorError err = client -> send(structArray);
        if (err != ()) {
        io:println("Error at helloWorld : " + err.message);
        }
        _ = client -> complete();
    }
}

type TestInt {
    int[] values;
};

type TestString {
    string[] values;
};

type TestFloat {
    float[] values;
};

type TestBoolean {
    boolean[] values;
};

type TestStruct {
    A[] values;
};

type A {
    string name;
};
