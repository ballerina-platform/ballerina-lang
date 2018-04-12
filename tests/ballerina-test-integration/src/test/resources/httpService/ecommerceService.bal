import ballerina/io;
import ballerina/mime;
import ballerina/http;

endpoint http:Listener serviceEndpoint {
    port:9090
};

@http:ServiceConfig {
    basePath:"/customerservice"
}
service<http:Service> CustomerMgtService bind serviceEndpoint {

    @http:ResourceConfig {
        methods:["GET", "POST"]
    }
    customers (endpoint outboundEP, http:Request req) {
        json payload = {};
        string httpMethod = req.method;
        if (httpMethod.equalsIgnoreCase("GET")) {
            payload = {"Customer":{"ID":"987654", "Name":"ABC PQR", "Description":"Sample Customer."}};
        } else {
            payload = {"Status":"Customer is successfully added."};
        }

        http:Response res = new;
        res.setJsonPayload(payload);
        _ = outboundEP -> respond(res);
    }
}

endpoint http:Client productsService {
    targets:[{url: "http://localhost:9090"}]
};

@http:ServiceConfig {
    basePath:"/ecommerceservice"
}
service<http:Service> Ecommerce bind serviceEndpoint {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/products/{prodId}"
    }
    productsInfo (endpoint outboundEP, http:Request req, string prodId) {
        string reqPath = "/productsservice/" + prodId;
        http:Request clientRequest = new;
        var clientResponse = productsService -> get(untaint reqPath, clientRequest);

        match clientResponse {
            http:HttpConnectorError err => {
                io:println("Error occurred while reading product response");
            }
            http:Response product => {
                _ = outboundEP -> respond(product);
            }
        }

    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/products"
    }
    productMgt (endpoint outboundEP, http:Request req) {
        http:Request clientRequest = new;
        var jsonReq = req.getJsonPayload();
        match jsonReq {
            http:PayloadError err => {
                io:println("Error occurred while reading products payload");
            }
            json products => {
                clientRequest.setJsonPayload(products);
            }
        }

        http:Response clientResponse = new;
        var clientRes = productsService -> post("/productsservice", clientRequest);
        match clientRes {
            http:HttpConnectorError err => {
                io:println("Error occurred while reading locator response");
            }
            http:Response prod => {
                clientResponse = prod;
            }
        }
        _ = outboundEP -> respond(clientResponse);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/orders"
    }
    ordersInfo (endpoint outboundEP, http:Request req) {
        http:Request clientRequest = new;
        var clientResponse = productsService -> get("/orderservice/orders", clientRequest);
        match clientResponse {
            http:HttpConnectorError err => {
                io:println("Error occurred while reading orders response");
            }
            http:Response orders => {
                _ = outboundEP -> respond(orders);
            }
        }
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/orders"
    }
    ordersMgt (endpoint outboundEP, http:Request req) {
        http:Request clientRequest = new;
        var clientResponse = productsService -> post("/orderservice/orders", clientRequest);
        match clientResponse {
            http:HttpConnectorError err => {
                io:println("Error occurred while writing orders response");
            }
            http:Response orders => {
                _ = outboundEP -> respond(orders);
            }
        }

    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/customers"
    }
    customersInfo (endpoint outboundEP, http:Request req) {
        http:Request clientRequest = new;
        var clientResponse = productsService -> get("/customerservice/customers", clientRequest);
        match clientResponse {
            http:HttpConnectorError err => {
                io:println("Error occurred while reading customers response");
            }
            http:Response customer => {
                _ = outboundEP -> respond(customer);
            }
        }

    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/customers"
    }
    customerMgt (endpoint outboundEP, http:Request req) {
        http:Request clientRequest = new;
        var clientResponse = productsService -> post("/customerservice/customers", clientRequest);
        match clientResponse {
            http:HttpConnectorError err => {
                io:println("Error occurred while writing customers response");
            }
            http:Response customer => {
                _ = outboundEP -> respond(customer);
            }
        }
    }
}

@http:ServiceConfig {
    basePath:"/orderservice"
}
service<http:Service> OrderMgtService bind serviceEndpoint {

    @http:ResourceConfig {
        methods:["GET", "POST"]
    }
    orders (endpoint outboundEP, http:Request req) {
        json payload = {};
        string httpMethod = req.method;
        if (httpMethod.equalsIgnoreCase("GET")) {
            payload = {"Order":{"ID":"111999", "Name":"ABC123", "Description":"Sample order."}};
        } else {
            payload = {"Status":"Order is successfully added."};
        }

        http:Response res = new;
        res.setJsonPayload(payload);
        _ = outboundEP -> respond(res);
    }
}

@http:ServiceConfig {
    basePath:"/productsservice"
}
service<http:Service> productmgt bind serviceEndpoint {

    map productsMap = populateSampleProducts();

    @http:ResourceConfig {
        methods:["GET"],
        path:"/{prodId}"
    }
    product (endpoint outboundEP, http:Request req, string prodId) {
        json payload = {};
        payload = check <json>productsMap[prodId];

        http:Response res = new;
        res.setJsonPayload(payload);
        _ = outboundEP -> respond(res);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/"
    }
    addProduct (endpoint outboundEP, http:Request req) {
        var jsonReq = req.getJsonPayload();

        match jsonReq {
            http:PayloadError err => {
                io:println("Error occurred while reading bank locator request");
            }
            json prod => {
                string productId = extractFieldValue(prod.Product.ID);
                productsMap[productId] = prod;
                json payload = {"Status":"Product is successfully added."};

                http:Response res = new;
                res.setJsonPayload(payload);
                _ = outboundEP -> respond(res);
            }
        }
    }
}

function populateSampleProducts () returns (map) {
    map productsMap;
    json prod_1 = {"Product":{"ID":"123000", "Name":"ABC_1", "Description":"Sample product."}};
    json prod_2 = {"Product":{"ID":"123001", "Name":"ABC_2", "Description":"Sample product."}};
    json prod_3 = {"Product":{"ID":"123002", "Name":"ABC_3", "Description":"Sample product."}};
    productsMap["123000"] = prod_1;
    productsMap["123001"] = prod_2;
    productsMap["123002"] = prod_3;
    io:println("Sample products are added.");
    return productsMap;
}

//Keep this until there's a simpler way to get a string value out of a json
function extractFieldValue(json fieldValue) returns string {
    match fieldValue {
        int i => return "error";
        string s => return s;
        boolean b => return "error";
        ()  => return "error";
        json j => return "error";
    }
}