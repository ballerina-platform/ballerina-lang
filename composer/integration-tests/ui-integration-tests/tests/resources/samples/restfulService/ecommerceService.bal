import ballerina/lang.messages;
import ballerina/http;
import ballerina/lang.system;
import ballerina/lang.strings;
import ballerina/lang.jsons;
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
@http:BasePath("/productsservice")
service productmgt {
    map productsMap = populateSampleProducts();
    
    @http:GET
    @http:Path ("/{id}")
    resource product(message m, 
    @PathParam("id") string prodId) {
        json payload = productsMap[prodId];
        message response = {};
        messages:setJsonPayload(response, payload);
        reply response;
        
    }
    
    @http:POST
    @http:Path ("/")
    resource product (message m) {
        json jsonReq = messages:getJsonPayload(m);
        string productId = jsons:getString(jsonReq, "$.Product.ID");
        productsMap[productId] = jsonReq;
        json payload = `{"Status":"Product is successfully added."}`;
        message response = {};
        messages:setJsonPayload(response, payload);
        reply response;
        
    }
    
}
function populateSampleProducts()(map productsMap) {
    productsMap = {};
    json prod_1 = `{"Product": {"ID": "123000", "Name": "ABC_1","Description": "Sample product."}}`;
    json prod_2 = `{"Product": {"ID": "123001", "Name": "ABC_2","Description": "Sample product."}}`;
    json prod_3 = `{"Product": {"ID": "123002", "Name": "ABC_3","Description": "Sample product."}}`;
    productsMap["123000"] = prod_1;
    productsMap["123001"] = prod_2;
    productsMap["123002"] = prod_3;
    system:println("Sample products are added.");
    return productsMap;
    
}
@http:BasePath("/orderservice")
service OrderMgtService {
    
    @http:GET
    @http:POST
    resource orders (message m) {
        json payload = {};
        string httpMethod = http:getMethod(m);
        if (strings:equalsIgnoreCase(httpMethod, "GET")) {
            payload = `{"Order": {"ID": "111999", "Name": "ABC123","Description": "Sample order."}}`;
            
        }
        else {
            payload = `{"Status":"Order is successfully added."}`;
            
        }
        message response = {};
        messages:setJsonPayload(response, payload);
        reply response;
        
    }
    
}
@http:BasePath("/customerservice")
service CustomerMgtService {
    
    @http:GET
    @http:POST
    resource customers (message m) {
        json payload = {};
        string httpMethod = http:getMethod(m);
        if (strings:equalsIgnoreCase(httpMethod, "GET")) {
            payload = `{"Customer": {"ID": "987654", "Name": "ABC PQR","Description": "Sample Customer."}}`;
            
        }
        else {
            payload = `{"Status":"Customer is successfully added."}`;
            
        }
        message response = {};
        messages:setJsonPayload(response, payload);
        reply response;
        
    }
    
}
