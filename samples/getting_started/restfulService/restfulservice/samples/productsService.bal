package restfulservice.samples;

import ballerina.net.http;

@http:configuration {basePath:"/productsservice"}
service<http> productmgt {

    map productsMap = populateSampleProducts();

    @http:resourceConfig {
        methods:["GET"],
        path:"/{prodId}"
    }
    resource product (http:Request req, http:Response res, string prodId) {
        json payload;
        payload, _ = (json)productsMap[prodId];
        res.setJsonPayload(payload);
        res.send();
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/"
    }
    resource addProduct (http:Request req, http:Response res) {
        json jsonReq = req.getJsonPayload();
        var productId, _ = (string)jsonReq.Product.ID;
        productsMap[productId] = jsonReq;
        json payload = {"Status":"Product is successfully added."};
        res.setJsonPayload(payload);
        res.send();
    }
}

function populateSampleProducts () (map productsMap) {
    productsMap = {};
    json prod_1 = {"Product":{"ID":"123000", "Name":"ABC_1", "Description":"Sample product."}};
    json prod_2 = {"Product":{"ID":"123001", "Name":"ABC_2", "Description":"Sample product."}};
    json prod_3 = {"Product":{"ID":"123002", "Name":"ABC_3", "Description":"Sample product."}};
    productsMap["123000"] = prod_1;
    productsMap["123001"] = prod_2;
    productsMap["123002"] = prod_3;
    println("Sample products are added.");
    return productsMap;
}
