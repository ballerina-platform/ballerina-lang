package restfulservice.samples;
import ballerina.net.http;
@http:BasePath ("/ecommerceservice")
service Ecommerce {
    http:ClientConnector productsService = create http:ClientConnector("http://localhost:9090");
    
    @http:GET
    @http:Path ("/products/{productId}")
    resource productsInfo(message m, 
    @http:PathParam("productId") string prodId) {
        string reqPath = "/productsservice/" + prodId;
        message response = http:ClientConnector.get(productsService, reqPath, m);
        reply response;
    
    }
    
    @http:POST
    @http:Path ("/products")
    resource productMgt (message m) {
        message response = http:ClientConnector.post(productsService, "/productsservice", m);
        reply response;
        
    }
    
    @http:GET
    @http:Path ("/orders")
    resource ordersInfo (message m) {
        http:ClientConnector productsService = create http:ClientConnector("http://localhost:9090");
        message response = http:ClientConnector.get(productsService, "/orderservice", m);
        reply response;
        
    }
    
    @http:POST
    @http:Path ("/orders")
    resource ordersMgt (message m) {
        http:ClientConnector productsService = create http:ClientConnector("http://localhost:9090");
        message response = http:ClientConnector.post(productsService, "/orderservice", m);
        reply response;
        
    }
    
    @http:GET
    @http:Path ("/customers")
    resource customersInfo (message m) {
        http:ClientConnector productsService = create http:ClientConnector("http://localhost:9090");
        message response = http:ClientConnector.get(productsService, "/customerservice", m);
        reply response;
        
    }
    
    @http:POST
    @http:Path ("/customers")
    resource customerMgt (message m) {
        http:ClientConnector productsService = create http:ClientConnector("http://localhost:9090");
        message response = http:ClientConnector.post(productsService, "/customerservice", m);
        reply response;
        
    }
    
}
