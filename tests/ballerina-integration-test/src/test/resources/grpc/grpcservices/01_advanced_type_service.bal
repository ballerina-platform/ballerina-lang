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
import ballerina/io;

endpoint grpc:Listener ep {
    host:"localhost",
    port:9090
};

service HelloWorld bind ep {

    testInputNestedStruct(endpoint caller, Person req) {
        io:println("name: " + req.name);
        io:println(req.address);
        string message = "Submitted name: " + req.name;
        io:println("Response message " + message);
        error? err = caller->send(message);
        if (err is error) {
            io:println("Error from Connector: " + err.reason());
        }
        _ = caller->complete();
    }

    testOutputNestedStruct(endpoint caller, string name) {
        io:println("requested name: " + name);
        Person person = {name:"Sam", address:{postalCode:10300, state:"CA", country:"USA"}};
        io:println(person);
        error? err = caller->send(person);
        if (err is error) {
            io:println("Error from Connector: " + err.reason());
        }
        _ = caller->complete();
    }

    testInputStructOutputStruct(endpoint caller, StockRequest req) {
        io:println("Getting stock details for symbol: " + req.name);
        StockQuote res = {symbol:"WSO2", name:"WSO2.com", last:149.52, low:150.70, high:
        149.18};
        io:println(res);
        error? err = caller->send(res);
        if (err is error) {
            io:println("Error from Connector: " + err.reason());
        }
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
        StockQuote res = {symbol:"WSO2", name:"WSO2 Inc.", last:14.0, low:15.0, high:16.0};
        StockQuote res1 = {symbol:"Google", name:"Google Inc.", last:100.0, low:101.0, high:102.0};
        StockQuotes quotes = {stock:[res, res1]};

        error? err = caller->send(quotes);
        if (err is error) {
            io:println("Error from Connector: " + err.reason());
        }
        _ = caller->complete();
    }

    testNoInputOutputArray(endpoint caller) {
        string[] names = ["WSO2", "Google"];
        StockNames stockNames = {names:names};
        error? err = caller->send(stockNames);
        if (err is error) {
            io:println("Error from Connector: " + err.reason());
        }
        _ = caller->complete();
    }
}

type Person record {
    string name;
    Address address;
};

type Address record {
    int postalCode;
    string state;
    string country;
};

type StockQuote record {
    string symbol;
    string name;
    float last;
    float low;
    float high;
};

type StockRequest record {
    string name;
};

type StockQuotes record {
    StockQuote[] stock;
};

type StockNames record {
    string[] names;
};
