
import ballerina/io;
import ballerina/http;

@http:configuration {basePath:"/productsservice"}
service<http> productmgt {

    map productsMap = populateSampleProducts();

    @http:resourceConfig {
        methods:["GET"],
        path:"/{prodId}"
    }
    resource product (http:Connection conn, http:Request req, string prodId) {
        json payload;
        payload, _ = (json)productsMap[prodId];

        http:Response res = {};
        res.setJsonPayload(payload);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/"
    }
    resource addProduct (http:Connection conn, http:Request req) {
        var jsonReq, payloadError = req.getJsonPayload();
        json payload;
        if (payloadError == null) {
            var productId, _ = (string)jsonReq.Product.ID;
            productsMap[productId] = jsonReq;
            payload = {"Status":"Product is successfully added."};
        } else {
            payload = {"Status":"An error occurred while retrieving json payload."};
        }
        http:Response res = {};
        res.setJsonPayload(payload);
        _ = conn.respond(res);
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
    io:println("Sample products are added.");
    return productsMap;
}
