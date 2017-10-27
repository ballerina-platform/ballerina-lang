package restfulservice.samples;

import ballerina.net.http;

@http:configuration {basePath:"/ecommerceservice"}
service<http> Ecommerce {

    http:ClientConnector productsService = create http:ClientConnector("http://localhost:9090", {});

    @http:resourceConfig {
        methods:["GET"],
        path:"/products/{prodId}"
    }
    resource productsInfo (http:Request req, http:Response resp, string prodId) {
        string reqPath = "/productsservice/" + prodId;
        http:Response clientResponse = productsService.get(reqPath, req);
        resp.forward(clientResponse);
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/products"
    }
    resource productMgt (http:Request req, http:Response resp) {
        http:Response clientResponse = productsService.post("/productsservice", req);
        resp.forward(clientResponse);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/orders"
    }
    resource ordersInfo (http:Request req, http:Response resp) {
        http:ClientConnector productsService = create http:ClientConnector("http://localhost:9090", {});
        http:Response clientResponse = productsService.get("/orderservice/orders", req);
        resp.forward(clientResponse);
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/orders"
    }
    resource ordersMgt (http:Request req, http:Response resp) {
        http:Response clientResponse = productsService.post("/orderservice/orders", req);
        resp.forward(clientResponse);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/customers"
    }
    resource customersInfo (http:Request req, http:Response resp) {
        http:Response clientResponse = productsService.get("/customerservice/customers", req);
        resp.forward(clientResponse);
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/customers"
    }
    resource customerMgt (http:Request req, http:Response resp) {
        http:Response clientResponse = productsService.post("/customerservice/customers", req);
        resp.forward(clientResponse);
    }
}
