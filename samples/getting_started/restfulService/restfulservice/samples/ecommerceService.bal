package restfulservice.samples;

import ballerina.net.http;

@http:configuration {basePath:"/ecommerceservice"}
service<http> Ecommerce {
    http:ClientConnector productsService = create http:ClientConnector("http://localhost:9090");

    @http:resourceConfig {
        methods:["GET"],
        path:"/products/{productId}"
    }
    resource productsInfo(message m,
    @http:PathParam{value:"productId"} string prodId) {
        string reqPath = "/productsservice/" + prodId;
        message response = productsService.get(reqPath, m);
        reply response;
    
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/products"
    }
    resource productMgt (message m) {
        message response = productsService.post("/productsservice", m);
        reply response;
        
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/orders"
    }
    resource ordersInfo (message m) {
        http:ClientConnector productsService = create http:ClientConnector("http://localhost:9090");
        message response = productsService.get("/orderservice/orders", m);
        reply response;
        
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/orders"
    }
    resource ordersMgt (message m) {
        http:ClientConnector productsService = create http:ClientConnector("http://localhost:9090");
        message response = productsService.post("/orderservice/orders", m);
        reply response;
        
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/customers"
    }
    resource customersInfo (message m) {
        http:ClientConnector productsService = create http:ClientConnector("http://localhost:9090");
        message response = productsService.get("/customerservice/customers", m);
        reply response;
        
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/customers"
    }
    resource customerMgt (message m) {
        http:ClientConnector productsService = create http:ClientConnector("http://localhost:9090");
        message response = productsService.post("/customerservice/customers", m);
        reply response;
        
    }
    
}
