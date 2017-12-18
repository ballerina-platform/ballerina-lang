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
    resource productsInfo (http:Connection con, http:Request req, string prodId) {
        string reqPath = "/productsservice/" + prodId;
        http:Response clientResponse = {};
        clientResponse, err = productsService.get(reqPath, req);
        _ = con.respond(clientResponse);
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/products"
    }
    resource productMgt (http:Connection con, http:Request req) {
        http:Response clientResponse = {};
        clientResponse, err = productsService.post("/productsservice", req);
        _ = con.respond(clientResponse);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/orders"
    }
    resource ordersInfo (http:Connection con, http:Request req) {
        endpoint<http:HttpClient> productsService {
            create http:HttpClient("http://localhost:9090", {});
        }
        http:Response clientResponse = {};
        clientResponse, err = productsService.get("/orderservice/orders", req);
        _ = con.respond(clientResponse);
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/orders"
    }
    resource ordersMgt (http:Connection con, http:Request req) {
        http:Response clientResponse = {};
        clientResponse, err = productsService.post("/orderservice/orders", req);
        _ = con.respond(clientResponse);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/customers"
    }
    resource customersInfo (http:Connection con, http:Request req) {
        http:Response clientResponse = {};
        clientResponse, err = productsService.get("/customerservice/customers", req);
        _ = con.respond(clientResponse);
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/customers"
    }
    resource customerMgt (http:Connection con, http:Request req) {
        http:Response clientResponse = {};
        clientResponse, err = productsService.post("/customerservice/customers", req);
        _ = con.respond(clientResponse);
    }
}
