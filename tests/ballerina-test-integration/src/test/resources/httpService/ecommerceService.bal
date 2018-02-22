import ballerina.io;
import ballerina.net.http;

@http:configuration {basePath:"/customerservice"}
service<http> CustomerMgtService {

    @http:resourceConfig {
        methods:["GET", "POST"]
    }
    resource customers (http:Connection conn, http:InRequest req) {
        json payload = {};
        string httpMethod = req.method;
        if (httpMethod.equalsIgnoreCase("GET")) {
            payload = {"Customer":{"ID":"987654", "Name":"ABC PQR", "Description":"Sample Customer."}};
        } else {
            payload = {"Status":"Customer is successfully added."};
        }

        http:OutResponse res = {};
        res.setJsonPayload(payload);
        _ = conn.respond(res);
    }
}

@http:configuration {basePath:"/ecommerceservice"}
service<http> Ecommerce {

    endpoint<http:HttpClient> productsService {
        create http:HttpClient("http://localhost:9090", {});
    }
    http:HttpConnectorError err;

    @http:resourceConfig {
        methods:["GET"],
        path:"/products/{prodId}"
    }
    resource productsInfo (http:Connection conn, http:InRequest req, string prodId) {
        string reqPath = "/productsservice/" + prodId;
        http:OutRequest clientRequest = {};
        var clientResponse, _ = productsService.get(reqPath, clientRequest);
        _ = conn.forward(clientResponse);
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/products"
    }
    resource productMgt (http:Connection conn, http:InRequest req) {
        http:OutRequest clientRequest = {};
        json jsonReq = req.getJsonPayload();
        clientRequest.setJsonPayload(jsonReq);
        var clientResponse, _ = productsService.post("/productsservice", clientRequest);
        _ = conn.forward(clientResponse);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/orders"
    }
    resource ordersInfo (http:Connection conn, http:InRequest req) {
        http:OutRequest clientRequest = {};
        var clientResponse, _ = productsService.get("/orderservice/orders", clientRequest);
        _ = conn.forward(clientResponse);
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/orders"
    }
    resource ordersMgt (http:Connection conn, http:InRequest req) {
        http:OutRequest clientRequest = {};
        var clientResponse, _ = productsService.post("/orderservice/orders", clientRequest);
        _ = conn.forward(clientResponse);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/customers"
    }
    resource customersInfo (http:Connection conn, http:InRequest req) {
        http:OutRequest clientRequest = {};
        var clientResponse, _ = productsService.get("/customerservice/customers", clientRequest);
        _ = conn.forward(clientResponse);
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/customers"
    }
    resource customerMgt (http:Connection conn, http:InRequest req) {
        http:OutRequest clientRequest = {};
        var clientResponse, _ = productsService.post("/customerservice/customers", clientRequest);
        _ = conn.forward(clientResponse);
    }
}

@http:configuration {basePath:"/orderservice"}
service<http> OrderMgtService {

    @http:resourceConfig {
        methods:["GET", "POST"]
    }
    resource orders (http:Connection conn, http:InRequest req) {
        json payload = {};
        string httpMethod = req.method;
        if (httpMethod.equalsIgnoreCase("GET")) {
            payload = {"Order":{"ID":"111999", "Name":"ABC123", "Description":"Sample order."}};
        } else {
            payload = {"Status":"Order is successfully added."};
        }

        http:OutResponse res = {};
        res.setJsonPayload(payload);
        _ = conn.respond(res);
    }
}

@http:configuration {basePath:"/productsservice"}
service<http> productmgt {

    map productsMap = populateSampleProducts();

    @http:resourceConfig {
        methods:["GET"],
        path:"/{prodId}"
    }
    resource product (http:Connection conn, http:InRequest req, string prodId) {
        json payload;
        payload, _ = (json)productsMap[prodId];

        http:OutResponse res = {};
        res.setJsonPayload(payload);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/"
    }
    resource addProduct (http:Connection conn, http:InRequest req) {
        json jsonReq = req.getJsonPayload();
        var productId, _ = (string)jsonReq.Product.ID;
        productsMap[productId] = jsonReq;
        json payload = {"Status":"Product is successfully added."};

        http:OutResponse res = {};
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