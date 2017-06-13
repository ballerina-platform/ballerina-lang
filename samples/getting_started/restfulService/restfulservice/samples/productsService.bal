package restfulservice.samples;

import ballerina.lang.messages;
import ballerina.lang.system;
import ballerina.lang.jsons;
import ballerina.net.http;

@http:BasePath{value:"/productsservice"}
service productmgt {
    map productsMap = populateSampleProducts();

    @http:GET{}
    @http:Path {value:"/{id}"}
    resource product(message m, @http:PathParam{value:"id"} string prodId) {
        json payload = (json) productsMap[prodId];
        message response = {};
        messages:setJsonPayload(response, payload);
        reply response;

    }

    @http:POST{}
    @http:Path {value:"/"}
    resource addProduct (message m) {
        json jsonReq = messages:getJsonPayload(m);
        string productId = jsons:getString(jsonReq, "$.Product.ID");
        productsMap[productId] = jsonReq;
        json payload = {"Status":"Product is successfully added."};
        message response = {};
        messages:setJsonPayload(response, payload);
        reply response;

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
