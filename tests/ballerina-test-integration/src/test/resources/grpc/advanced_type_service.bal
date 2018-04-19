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

endpoint grpc:Listener ep {
    host:"localhost",
    port:9090
};

@grpc:serviceConfig
service HelloWorld bind ep {

    testInputNestedStruct(endpoint caller, Person req) {
        io:println("name: " + req.name);
        io:println(req.address);
        string message = "Submitted name: " + req.name;
        error? err = caller->send(message);
        io:println(err.message but { () => ("Server send response : " + message) });
        _ = caller->complete();
    }

    testOutputNestedStruct(endpoint caller, string name) {
        io:println("requested name: " + name);
        Person person = {name:"Sam", address:{postalCode:10300, state:"CA", country:"USA"}};
        io:println(person);
        error? err = caller->send(person);
        io:println(err.message but { () => "" });
        _ = caller->complete();
    }

    testInputStructOutputStruct(endpoint caller, StockRequest req) {
        io:println("Getting stock details for symbol: " + req.name);
        StockQuote res = {symbol:"WSO2", name:"WSO2.com", last:149.52, low:150.70, high:
        149.18};
        io:println(res);
        error? err = caller->send(res);
        io:println(err.message but { () => "" });
        _ = caller->complete();
    }

    testInputStructNoOutput(endpoint caller, StockQuote req) {
        io:println("Symbol: " + req.symbol);
        io:println("Name: " + req.name);
        io:println("Last: " + req.last);
        io:println("Low: " + req.low);
        io:println("High: " + req.high);
    }

    testNoInputOutputStruct(endpoint caller) {
        StockQuote res = {symbol:"WSO2", name:"WSO2 Inc.", last:14, low:15, high:16};
        StockQuote res1 = {symbol:"Google", name:"Google Inc.", last:100, low:101, high:102};
        StockQuotes quotes = {stock:[res, res1]};
        io:println(quotes);

        error? err = caller->send(quotes);
        io:println(err.message but { () => "" });
        _ = caller->complete();
    }

    testNoInputOutputArray(endpoint caller) {
        string[] names = ["WSO2", "Google"];
        io:println(names);
        StockNames stockNames = {names:names};
        error? err = caller->send(stockNames);
        io:println(err.message but { () => "" });
        _ = caller->complete();
    }
}

type Person {
    string name;
    Address address;
};

type Address {
    int postalCode;
    string state;
    string country;
};

type StockQuote {
    string symbol;
    string name;
    float last;
    float low;
    float high;
};

type StockRequest {
    string name;
};

type StockQuotes {
    StockQuote[] stock;
};

type StockNames {
    string[] names;
};
