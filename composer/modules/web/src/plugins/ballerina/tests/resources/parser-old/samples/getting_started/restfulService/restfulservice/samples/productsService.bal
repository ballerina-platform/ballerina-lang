package restfulservice.samples;

import ballerina/lang.messages;
import ballerina/lang.system;
import ballerina/http;

@http:configuration {basePath:"/productsservice"}
service<http> productmgt {
    map productsMap = populateSampleProducts();

    @http:resourceConfig {
        methods:["GET"],
        path:"/{id}"
    }
    resource product(message m, @http:PathParam{value:"id"} string prodId) {
        json payload;
        payload, _ = (json) productsMap[prodId];
        message response = {};
        messages:setJsonPayload(response, payload);
        response:send(response);

    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/"
    }
    resource addProduct (message m) {
        json jsonReq = messages:getJsonPayload(m);
        var productId,_ = (string) jsonReq.Product.ID;
        productsMap[productId] = jsonReq;
        json payload = {"Status":"Product is successfully added."};
        message response = {};
        messages:setJsonPayload(response, payload);
        response:send(response);

    }

}

function populateSampleProducts()(map productsMap) {
    productsMap = {};
    json prod_1 = {"Product": {"ID": "123000", "Name": "ABC_1","Description": "Sample product."}};
    json prod_2 = {"Product": {"ID": "123001", "Name": "ABC_2","Description": "Sample product."}};
    json prod_3 = {"Product": {"ID": "123002", "Name": "ABC_3","Description": "Sample product."}};
    productsMap["123000"] = prod_1;
    productsMap["123001"] = prod_2;
    productsMap["123002"] = prod_3;
    system:println("Sample products are added.");
    return productsMap;

}
