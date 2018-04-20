
import ballerina/http;

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
    resource productsInfo (http:Connection conn, http:Request req, string prodId) {
        string reqPath = "/productsservice/" + prodId;
        http:Request clientRequest = {};
        var clientResponse, _ = productsService.get(reqPath, clientRequest);
        _ = conn.forward(clientResponse);
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/products"
    }
    resource productMgt (http:Connection conn, http:Request req) {
        http:Request clientRequest = {};
        var jsonReq, payloadError = req.getJsonPayload();
        if (payloadError == null) {
            clientRequest.setJsonPayload(jsonReq);
        } else {
            clientRequest.setStringPayload(payloadError.message);
        }

        var clientResponse, _ = productsService.post("/productsservice", clientRequest);
        _ = conn.forward(clientResponse);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/orders"
    }
    resource ordersInfo (http:Connection conn, http:Request req) {
        http:Request clientRequest = {};
        var clientResponse, _ = productsService.get("/orderservice/orders", clientRequest);
        _ = conn.forward(clientResponse);
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/orders"
    }
    resource ordersMgt (http:Connection conn, http:Request req) {
        http:Request clientRequest = {};
        var clientResponse, _ = productsService.post("/orderservice/orders", clientRequest);
        _ = conn.forward(clientResponse);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/customers"
    }
    resource customersInfo (http:Connection conn, http:Request req) {
        http:Request clientRequest = {};
        var clientResponse, _ = productsService.get("/customerservice/customers", clientRequest);
        _ = conn.forward(clientResponse);
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/customers"
    }
    resource customerMgt (http:Connection conn, http:Request req) {
        http:Request clientRequest = {};
        var clientResponse, _ = productsService.post("/customerservice/customers", clientRequest);
        _ = conn.forward(clientResponse);
    }
}
