package restfulservice.samples;

import ballerina.net.http;

@http:BasePath{value: "/ecommerceservice"}
service Ecommerce {
    http:ClientConnector productsService = create http:ClientConnector("http://localhost:9090");

    @http:GET{}
    @http:Path{value: "/products/{productId}"}
    resource productsInfo() {
        string reqPath = "/productsservice/" + undefined;
        message response = http:ClientConnector.get(undefined, undefined);

        reply response;
    }

    @http:POST{}
    @http:Path{value: "/products"}
    resource productMgt() {
        message response = http:ClientConnector.post("/productsservice", undefined);

        reply response;
    }

    @http:GET{}
    @http:Path{value: "/orders"}
    resource ordersInfo() {
        http:ClientConnector productsService = create http:ClientConnector("http://localhost:9090");
        message response = http:ClientConnector.get("/orderservice", undefined);

        reply response;
    }

    @http:POST{}
    @http:Path{value: "/orders"}
    resource ordersMgt() {
        http:ClientConnector productsService = create http:ClientConnector("http://localhost:9090");
        message response = http:ClientConnector.post("/orderservice", undefined);

        reply response;
    }

    @http:GET{}
    @http:Path{value: "/customers"}
    resource customersInfo() {
        http:ClientConnector productsService = create http:ClientConnector("http://localhost:9090");
        message response = http:ClientConnector.get("/customerservice", undefined);

        reply response;
    }

    @http:POST{}
    @http:Path{value: "/customers"}
    resource customerMgt() {
        http:ClientConnector productsService = create http:ClientConnector("http://localhost:9090");
        message response = http:ClientConnector.post("/customerservice", undefined);

        reply response;
    }
}
