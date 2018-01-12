package restfulservice.samples;

import ballerina.net.http;

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
    resource productsInfo (http:Request req, http:Response resp, string prodId) {
        string reqPath = "/productsservice/" + prodId;
        http:Response clientResponse = {};
        clientResponse, err = productsService.get(reqPath, req);
        _ = resp.forward(clientResponse);
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/products"
    }
    resource productMgt (http:Request req, http:Response resp) {
        http:Response clientResponse = {};
        clientResponse, err = productsService.post("/productsservice", req);
        _ = resp.forward(clientResponse);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/orders"
    }
    resource ordersInfo (http:Request req, http:Response resp) {
        endpoint<http:HttpClient> productsService {
            create http:HttpClient("http://localhost:9090", {});
        }
        http:Response clientResponse = {};
        clientResponse, err = productsService.get("/orderservice/orders", req);
        _ = resp.forward(clientResponse);
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/orders"
    }
    resource ordersMgt (http:Request req, http:Response resp) {
        http:Response clientResponse = {};
        clientResponse, err = productsService.post("/orderservice/orders", req);
        _ = resp.forward(clientResponse);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/customers"
    }
    resource customersInfo (http:Request req, http:Response resp) {
        http:Response clientResponse = {};
        clientResponse, err = productsService.get("/customerservice/customers", req);
        _ = resp.forward(clientResponse);
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/customers"
    }
    resource customerMgt (http:Request req, http:Response resp) {
        http:Response clientResponse = {};
        clientResponse, err = productsService.post("/customerservice/customers", req);
        _ = resp.forward(clientResponse);
    }
}
