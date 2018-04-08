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
service<grpc:Endpoint> HelloWorld bind ep {

    testInputNestedStruct (endpoint client, Person req) {
        io:println("name: " + req.name);
        io:println(req.address);
        string message = "Submitted name: " + req.name;
        grpc:ConnectorError err = client -> send(message);
        io:println("Server send response : " + message );
        if (err != ()) {
            io:println("Error at helloWorld : " + err.message);
        }
        _ = client -> complete();
    }

    testOutputNestedStruct (endpoint client, string name) {
        io:println("requested name: " + name);
        Person person = {name: "Sam", address: {postalCode:10300, state:"CA", country:"USA"}};
        io:println(person);
        grpc:ConnectorError err = client -> send(person);
        if (err != ()) {
            io:println("Error at helloWorld : " + err.message);
        }
        _ = client -> complete();
    }

    testInputStructOutputStruct (endpoint client, StockRequest req) {
        io:println("Getting stock details for symbol: " + req.name);
        StockQuote res = {symbol: "IBM", name: "International Business Machines", last: 149.52, low: 150.70, high:
        149.18};
        io:println(res);
        grpc:ConnectorError err = client -> send(res);
        if (err != ()) {
            io:println("Error at helloWorld : " + err.message);
        }
        _ = client -> complete();
    }

    testInputStructNoOutput (endpoint client, StockQuote req) {
        io:println("Symbol: " + req.symbol);
        io:println("Name: " + req.name);
        io:println("Last: " + req.last);
        io:println("Low: " + req.low);
        io:println("High: " + req.high);
        _ = client -> complete();
    }

    testNoInputOutputStruct(endpoint client) {
        StockQuote res = {symbol: "WSO2", name: "WSO2 Inc.", last: 14, low: 15, high: 16};
        StockQuote res1 = {symbol: "Google", name: "Google Inc.", last: 100, low: 101, high: 102};
        StockQuotes quotes = {stock:[res,res1]};
        io:println(quotes);

        grpc:ConnectorError err = client -> send(quotes);
        if (err != ()) {
            io:println("Error at helloWorld : " + err.message);
        }
        _ = client -> complete();
    }

    testNoInputOutputArray(endpoint client) {
        string[] names = ["WSO2", "Google"];
        io:println(names);

        grpc:ConnectorError err = client -> send(names);
        if (err != ()) {
            io:println("Error at helloWorld : " + err.message);
        }
        _ = client -> complete();
    }
}

type Person {
    string name;
    Address address;
}

type Address {
    int postalCode;
    string state;
    string country;
}

type StockQuote {
    string symbol;
    string name;
    float last;
    float low;
    float high;
}

type StockRequest {
    string name;
}

type StockQuotes {
    StockQuote[] stock;
}

type StockNames {
    string[] names;
}
