import ballerina/io;
import ballerina/mime;
import ballerina/http;

listener http:Listener serviceEndpoint5 = new(9095);

@http:ServiceConfig {
    basePath:"/customerservice"
}
service CustomerMgtService on serviceEndpoint5 {

    @http:ResourceConfig {
        methods:["GET", "POST"]
    }
    resource function customers(http:Caller caller, http:Request req) {
        json payload = {};
        string httpMethod = req.method;
        if (httpMethod.equalsIgnoreCase("GET")) {
            payload = {"Customer":{"ID":"987654", "Name":"ABC PQR", "Description":"Sample Customer."}};
        } else {
            payload = {"Status":"Customer is successfully added."};
        }

        http:Response res = new;
        res.setJsonPayload(payload);
        _ = caller->respond(res);
    }
}

http:Client productsService = new("http://localhost:9095");

@http:ServiceConfig {
    basePath:"/ecommerceservice"
}
service Ecommerce on serviceEndpoint5 {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/products/{prodId}"
    }
    resource function productsInfo(http:Caller caller, http:Request req, string prodId) {
        string reqPath = "/productsservice/" + untaint prodId;
        http:Request clientRequest = new;
        var clientResponse = productsService->get(untaint reqPath, message = clientRequest);
        if (clientResponse is http:Response) {
            _ = caller->respond(clientResponse);
        } else {
            io:println("Error occurred while reading product response");
        }
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/products"
    }
    resource function productMgt(http:Caller caller, http:Request req) {
        http:Request clientRequest = new;
        var jsonReq = req.getJsonPayload();
        if (jsonReq is json) {
            clientRequest.setPayload(untaint jsonReq);
        } else {
            io:println("Error occurred while reading products payload");
        }

        http:Response clientResponse = new;
        var clientRes = productsService->post("/productsservice", clientRequest);
        if (clientRes is http:Response) {
            clientResponse = clientRes;
        } else {
            io:println("Error occurred while reading locator response");
        }
        _ = caller->respond(clientResponse);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/orders"
    }
    resource function ordersInfo(http:Caller caller, http:Request req) {
        http:Request clientRequest = new;
        var clientResponse = productsService->get("/orderservice/orders", message = clientRequest);
        if (clientResponse is http:Response) {
            _ = caller->respond(clientResponse);
        } else {
            io:println("Error occurred while reading orders response");
        }
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/orders"
    }
    resource function ordersMgt(http:Caller caller, http:Request req) {
        http:Request clientRequest = new;
        var clientResponse = productsService->post("/orderservice/orders", clientRequest);
        if (clientResponse is http:Response) {
            _ = caller->respond(clientResponse);
        } else {
            io:println("Error occurred while writing orders respons");
        }
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/customers"
    }
    resource function customersInfo(http:Caller caller, http:Request req) {
        http:Request clientRequest = new;
        var clientResponse = productsService->get("/customerservice/customers", message = clientRequest);
        if (clientResponse is http:Response) {
            _ = caller->respond(clientResponse);
        } else {
            io:println("Error occurred while reading customers response");
        }
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/customers"
    }
    resource function customerMgt(http:Caller caller, http:Request req) {
        http:Request clientRequest = new;
        var clientResponse = productsService->post("/customerservice/customers", clientRequest);
        if (clientResponse is http:Response) {
            _ = caller->respond(clientResponse);
        } else {
            io:println("Error occurred while writing customers response");
        }
    }
}

@http:ServiceConfig {
    basePath:"/orderservice"
}
service OrderMgtService on serviceEndpoint5 {

    @http:ResourceConfig {
        methods:["GET", "POST"]
    }
    resource function orders(http:Caller caller, http:Request req) {
        json payload = {};
        string httpMethod = req.method;
        if (httpMethod.equalsIgnoreCase("GET")) {
            payload = {"Order":{"ID":"111999", "Name":"ABC123", "Description":"Sample order."}};
        } else {
            payload = {"Status":"Order is successfully added."};
        }

        http:Response res = new;
        res.setJsonPayload(payload);
        _ = caller->respond(res);
    }
}

@http:ServiceConfig {
    basePath:"/productsservice"
}
service productmgt on serviceEndpoint5 {

    map<any> productsMap = populateSampleProducts();

    @http:ResourceConfig {
        methods:["GET"],
        path:"/{prodId}"
    }
    resource function product(http:Caller caller, http:Request req, string prodId) {
        http:Response res = new;
        var result = json.convert(self.productsMap[prodId]);
        if (result is json) {
            res.setPayload(result);
        } else {
            res.setPayload(result.reason());
        }
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/"
    }
    resource function addProduct(http:Caller caller, http:Request req) {
        var jsonReq = req.getJsonPayload();
        if (jsonReq is json) {
            string productId = jsonReq.Product.ID.toString();
            self.productsMap[productId] = jsonReq;
            json payload = {"Status":"Product is successfully added."};

            http:Response res = new;
            res.setPayload(payload);
            _ = caller->respond(res);
        } else {
            io:println("Error occurred while reading bank locator request");
        }
    }
}

function populateSampleProducts() returns (map<any>) {
    map<any> productsMap = {};
    json prod_1 = {"Product":{"ID":"123000", "Name":"ABC_1", "Description":"Sample product."}};
    json prod_2 = {"Product":{"ID":"123001", "Name":"ABC_2", "Description":"Sample product."}};
    json prod_3 = {"Product":{"ID":"123002", "Name":"ABC_3", "Description":"Sample product."}};
    productsMap["123000"] = prod_1;
    productsMap["123001"] = prod_2;
    productsMap["123002"] = prod_3;
    io:println("Sample products are added.");
    return productsMap;
}

