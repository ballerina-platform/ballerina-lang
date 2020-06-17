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
import ballerina/java.jdbc;
import ballerina/observe;
import ballerina/sql;

jdbc:Client testDB = check new("jdbc:h2:file:../../tempdb/TEST_DB", "SA", "");

type Product record {
    int productId;
    string productName;
};

@http:ServiceConfig {
    basePath:"/test"
}
service metricsTest on new http:Listener(9898) {
    @http:ResourceConfig {
        path: "/"
    }
    resource function getProduct (http:Caller caller, http:Request req) {
        stream<record{}, error> resultsStream = testDB->query(getQuery(), Product);
        stream<Product, sql:Error> productsStream = <stream<Product, sql:Error>>resultsStream;

        Product[] products = [];
        error? e = productsStream.forEach(function(Product product) {
            products.push(product);
        });
        if (e is error) {
            error err = error ("error occurred while reading products stream");
            panic err;
        }
        e = productsStream.close();
        if (e is error) {
            error err = error ("error occurred while closing products stream");
            panic err;
        }

        string result = products.toString();
        http:Response resp = new;
        resp.setTextPayload(<@untainted> result);
        checkpanic caller->respond(resp);
    }
}

@observe:Observable
public function getQuery() returns string {
    return "SELECT * FROM Products";
}
