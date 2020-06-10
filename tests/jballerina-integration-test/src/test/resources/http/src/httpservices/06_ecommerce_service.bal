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
import ballerina/http;
import ballerina/stringutils;

listener http:Listener serviceEndpoint5 = new(9095);

@http:ServiceConfig {
    basePath:"/customerservice"
}
service CustomerMgtService on serviceEndpoint5 {

    @http:ResourceConfig {
        methods:["GET", "POST"]
    }
    resource function customers(http:Caller caller, http:Request req) {
        json payload = {};
        string httpMethod = req.method;
        if (stringutils:equalsIgnoreCase(httpMethod, "GET")) {
            payload = {"Customer":{"ID":"987654", "Name":"ABC PQR", "Description":"Sample Customer."}};
        } else {
            payload = {"Status":"Customer is successfully added."};
        }

        http:Response res = new;
        res.setJsonPayload(payload);
        checkpanic caller->respond(res);
    }
}

http:Client productsService = new("http://localhost:9095");

@http:ServiceConfig {
    basePath:"/ecommerceservice"
}
service Ecommerce on serviceEndpoint5 {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/products/{prodId}"
    }
    resource function productsInfo(http:Caller caller, http:Request req, string prodId) {
        string reqPath = "/productsservice/" + <@untainted> prodId;
        http:Request clientRequest = new;
        var clientResponse = productsService->get(<@untainted> reqPath, clientRequest);
        if (clientResponse is http:Response) {
            checkpanic caller->respond(clientResponse);
        } else {
            io:println("Error occurred while reading product response");
        }
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/products"
    }
    resource function productMgt(http:Caller caller, http:Request req) {
        http:Request clientRequest = new;
        var jsonReq = req.getJsonPayload();
        if (jsonReq is json) {
            clientRequest.setPayload(<@untainted> jsonReq);
        } else {
            io:println("Error occurred while reading products payload");
        }

        http:Response clientResponse = new;
        var clientRes = productsService->post("/productsservice", clientRequest);
        if (clientRes is http:Response) {
            clientResponse = clientRes;
        } else {
            io:println("Error occurred while reading locator response");
        }
        checkpanic caller->respond(clientResponse);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/orders"
    }
    resource function ordersInfo(http:Caller caller, http:Request req) {
        http:Request clientRequest = new;
        var clientResponse = productsService->get("/orderservice/orders", clientRequest);
        if (clientResponse is http:Response) {
            checkpanic caller->respond(clientResponse);
        } else {
            io:println("Error occurred while reading orders response");
        }
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/orders"
    }
    resource function ordersMgt(http:Caller caller, http:Request req) {
        http:Request clientRequest = new;
        var clientResponse = productsService->post("/orderservice/orders", clientRequest);
        if (clientResponse is http:Response) {
            checkpanic caller->respond(clientResponse);
        } else {
            io:println("Error occurred while writing orders respons");
        }
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/customers"
    }
    resource function customersInfo(http:Caller caller, http:Request req) {
        http:Request clientRequest = new;
        var clientResponse = productsService->get("/customerservice/customers", clientRequest);
        if (clientResponse is http:Response) {
            checkpanic caller->respond(clientResponse);
        } else {
            io:println("Error occurred while reading customers response");
        }
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/customers"
    }
    resource function customerMgt(http:Caller caller, http:Request req) {
        http:Request clientRequest = new;
        var clientResponse = productsService->post("/customerservice/customers", clientRequest);
        if (clientResponse is http:Response) {
            checkpanic caller->respond(clientResponse);
        } else {
            io:println("Error occurred while writing customers response");
        }
    }
}

@http:ServiceConfig {
    basePath:"/orderservice"
}
service OrderMgtService on serviceEndpoint5 {

    @http:ResourceConfig {
        methods:["GET", "POST"]
    }
    resource function orders(http:Caller caller, http:Request req) {
        json payload = {};
        string httpMethod = req.method;
        if (stringutils:equalsIgnoreCase(httpMethod, "GET")) {
            payload = {"Order":{"ID":"111999", "Name":"ABC123", "Description":"Sample order."}};
        } else {
            payload = {"Status":"Order is successfully added."};
        }

        http:Response res = new;
        res.setJsonPayload(payload);
        checkpanic caller->respond(res);
    }
}

@tainted map<anydata> productsMap = populateSampleProducts();

@http:ServiceConfig {
    basePath:"/productsservice"
}
service productmgt on serviceEndpoint5 {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/{prodId}"
    }
    resource function product(http:Caller caller, http:Request req, string prodId) {
        http:Response res = new;
        var result = productsMap[prodId].cloneWithType(json);
        if (result is json) {
            res.setPayload(<@untainted> result);
        } else {
            res.setPayload(<@untainted> result.reason());
        }
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/"
    }
    resource function addProduct(http:Caller caller, http:Request req) {
        var jsonReq = req.getJsonPayload();
        if (jsonReq is json) {
            string productId = jsonReq.Product.ID.toString();
            productsMap[productId] = jsonReq;
            json payload = {"Status":"Product is successfully added."};

            http:Response res = new;
            res.setPayload(payload);
            checkpanic caller->respond(res);
        } else {
            io:println("Error occurred while reading bank locator request");
        }
    }
}

function populateSampleProducts() returns (map<anydata>) {
    map<anydata> productsMap = {};
    json prod_1 = {"Product":{"ID":"123000", "Name":"ABC_1", "Description":"Sample product."}};
    json prod_2 = {"Product":{"ID":"123001", "Name":"ABC_2", "Description":"Sample product."}};
    json prod_3 = {"Product":{"ID":"123002", "Name":"ABC_3", "Description":"Sample product."}};
    productsMap["123000"] = prod_1;
    productsMap["123001"] = prod_2;
    productsMap["123002"] = prod_3;
    io:println("Sample products are added.");
    return productsMap;
}

