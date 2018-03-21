import ballerina/io;
import ballerina/net.http;

endpoint<http:Service> serviceEnpoint {
    port:9090
}

@http:serviceConfig {
    basePath:"/customerservice",
    endpoints:[serviceEnpoint]
}
service<http:Service> CustomerMgtService {

    @http:resourceConfig {
        methods:["GET", "POST"]
    }
    resource customers (http:ServerConnector conn, http:Request req) {
        json payload = {};
        string httpMethod = req.method;
        if (httpMethod.equalsIgnoreCase("GET")) {
            payload = {"Customer":{"ID":"987654", "Name":"ABC PQR", "Description":"Sample Customer."}};
        } else {
            payload = {"Status":"Customer is successfully added."};
        }

        http:Response res = {};
        res.setJsonPayload(payload);
        _ = conn -> respond(res);
    }
}

endpoint<http:Client> productsService {
    serviceUri: "http://localhost:9090"
}

@http:serviceConfig {
    basePath:"/ecommerceservice",
    endpoints:[serviceEnpoint]
}
service<http:Service> Ecommerce {
    http:HttpConnectorError err;

    @http:resourceConfig {
        methods:["GET"],
        path:"/products/{prodId}"
    }
    resource productsInfo (http:ServerConnector conn, http:Request req, string prodId) {
        string reqPath = "/productsservice/" + prodId;
        http:Request clientRequest = {};
        var clientResponse, _ = productsService -> get(reqPath, clientRequest);
        _ = conn -> forward(clientResponse);
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/products"
    }
    resource productMgt (http:ServerConnector conn, http:Request req) {
        http:Request clientRequest = {};
        var jsonReq, _ = req.getJsonPayload();
        clientRequest.setJsonPayload(jsonReq);
        var clientResponse, _ = productsService -> post("/productsservice", clientRequest);
        _ = conn -> forward(clientResponse);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/orders"
    }
    resource ordersInfo (http:ServerConnector conn, http:Request req) {
        http:Request clientRequest = {};
        var clientResponse, _ = productsService -> get("/orderservice/orders", clientRequest);
        _ = conn -> forward(clientResponse);
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/orders"
    }
    resource ordersMgt (http:ServerConnector conn, http:Request req) {
        http:Request clientRequest = {};
        var clientResponse, _ = productsService -> post("/orderservice/orders", clientRequest);
        _ = conn -> forward(clientResponse);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/customers"
    }
    resource customersInfo (http:ServerConnector conn, http:Request req) {
        http:Request clientRequest = {};
        var clientResponse, _ = productsService -> get("/customerservice/customers", clientRequest);
        _ = conn -> forward(clientResponse);
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/customers"
    }
    resource customerMgt (http:ServerConnector conn, http:Request req) {
        http:Request clientRequest = {};
        var clientResponse, _ = productsService -> post("/customerservice/customers", clientRequest);
        _ = conn -> forward(clientResponse);
    }
}

@http:serviceConfig {
    basePath:"/orderservice",
    endpoints:[serviceEnpoint]
}
service<http:Service> OrderMgtService {

    @http:resourceConfig {
        methods:["GET", "POST"]
    }
    resource orders (http:ServerConnector conn, http:Request req) {
        json payload = {};
        string httpMethod = req.method;
        if (httpMethod.equalsIgnoreCase("GET")) {
            payload = {"Order":{"ID":"111999", "Name":"ABC123", "Description":"Sample order."}};
        } else {
            payload = {"Status":"Order is successfully added."};
        }

        http:Response res = {};
        res.setJsonPayload(payload);
        _ = conn -> respond(res);
    }
}

@http:serviceConfig {
    basePath:"/productsservice",
    endpoints:[serviceEnpoint]
}
service<http:Service> productmgt {

    map productsMap = populateSampleProducts();

    @http:resourceConfig {
        methods:["GET"],
        path:"/{prodId}"
    }
    resource product (http:ServerConnector conn, http:Request req, string prodId) {
        json payload;
        payload, _ = (json)productsMap[prodId];

        http:Response res = {};
        res.setJsonPayload(payload);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/"
    }
    resource addProduct (http:ServerConnector conn, http:Request req) {
        var jsonReq, _ = req.getJsonPayload();
        var productId, _ = (string)jsonReq.Product.ID;
        productsMap[productId] = jsonReq;
        json payload = {"Status":"Product is successfully added."};

        http:Response res = {};
        res.setJsonPayload(payload);
        _ = conn -> respond(res);
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