import ballerina/io;
import ballerina/http;

endpoint http:NonListener testEP {
    port:9090
};

@http:ServiceConfig {
    basePath:"/ecommerceservice"
}
service<http:Service> Ecommerce bind testEP {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/products/{productId}/{regId}"
    }
     productsInfo1 (endpoint client, http:Request req, string productId, string regId) {
        string orderId = req.getHeader("X-ORDER-ID");
        io:println("Order ID " + orderId);
        io:println("Product ID " + productId);
        io:println("Reg ID " + regId);
        json responseJson = {"X-ORDER-ID":orderId, "ProductID":productId, "RegID":regId};
        io:println(responseJson.toString());

        http:Response res = new;
        res.setJsonPayload(responseJson);
        _ = client -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/products2/{productId}/{regId}/item"
    }
     productsInfo2 (endpoint client, http:Request req, string productId, string regId) {
        json responseJson;
        io:println("Product ID " + productId);
        io:println("Reg ID " + regId);
        responseJson = {"Template":"T2", "ProductID":productId, "RegID":regId};
        io:println(responseJson.toString());

        http:Response res = new;
        res.setJsonPayload(responseJson);
        _ = client -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/products3/{productId}/{regId}/*"
    }
     productsInfo3 (endpoint client, http:Request req, string productId, string regId) {
        json responseJson;
        io:println("Product ID " + productId);
        io:println("Reg ID " + regId);
        responseJson = {"Template":"T3", "ProductID":productId, "RegID":regId};
        io:println(responseJson.toString());

        http:Response res = new;
        res.setJsonPayload(responseJson);
        _ = client -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/products/{productId}"
    }
     productsInfo4 (endpoint client, http:Request req, string productId) {
        json responseJson;
        string rID = req.getQueryParams().regID;
        io:println("Product ID " + productId);
        io:println("Reg ID " + rID);
        responseJson = {"Template":"T4", "ProductID":productId, "RegID":rID};
        io:println(responseJson.toString());

        http:Response res = new;
        res.setJsonPayload(responseJson);
        _ = client -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/products"
    }
     productsInfo6 (endpoint client, http:Request req) {
        json responseJson;
        map<string> params = req.getQueryParams();
        string prdID = params.prodID;
        string rID= params.regID;
        io:println ("Product ID " + prdID);
        io:println ("Reg ID " + rID);
        responseJson = {"Template":"T6", "ProductID":prdID, "RegID":rID};
        io:println (responseJson.toString ());

        http:Response res = new;
        res.setJsonPayload(responseJson);
        _ = client -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/products5/{productId}/reg"
    }
     productsInfo5 (endpoint client, http:Request req, string productId) {
        json responseJson;
        string rID = req.getQueryParams().regID;
        io:println("Product ID " + productId);
        io:println("Reg ID " + rID);
        responseJson = {"Template":"T5", "ProductID":productId, "RegID":rID};
        io:println(responseJson.toString());

        http:Response res = new;
        res.setJsonPayload(responseJson);
        _ = client -> respond(res);
    }

    @http:ResourceConfig {
        path:""
    }
     echo1 (endpoint client, http:Request req) {
        http:Response res = new;
        json responseJson = {"echo11":"echo11"};
        res.setJsonPayload(responseJson);
        _ = client -> respond(res);
    }
}

@http:ServiceConfig {
    basePath:"/options"
}
service<http:Service> echo111 bind testEP {

    @http:ResourceConfig {
        methods:["POST", "UPDATE"],
        path : "/test"
    }
     productsInfo99 (endpoint client, http:Request req) {
        http:Response res = new;
        _ = client -> respond(res);
    }

    @http:ResourceConfig {
        methods:["OPTIONS"],
        path : "/hi"
    }
     productsOptions (endpoint client, http:Request req) {
        http:Response res = new;
        json responseJson = {"echo":"wso2"};
        res.setJsonPayload(responseJson);
        _ = client -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET", "PUT"],
        path : "/test"
    }
     productsInfo98 (endpoint client, http:Request req) {
        http:Response res = new;
        _ = client -> respond(res);

    }

    @http:ResourceConfig {
        methods:["GET"],
        path : "/getme"
    }
     productsGet (endpoint client, http:Request req) {
        http:Response res = new;
        json responseJson = {"echo":"get"};
        res.setJsonPayload(responseJson);
        _ = client -> respond(res);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path : "/post"
    }
     productsPOST (endpoint client, http:Request req) {
        http:Response res = new;
        json responseJson = {"echo":"post"};
        res.setJsonPayload(responseJson);
        _ = client -> respond(res);
    }

    @http:ResourceConfig {
        methods:["PUT"],
        path : "/put"
    }
     productsPUT (endpoint client, http:Request req) {
        http:Response res = new;
        json responseJson = {"echo":"put"};
        res.setJsonPayload(responseJson);
        _ = client -> respond(res);
    }
}

@http:ServiceConfig {
    basePath:"/noResource"
}
service<http:Service> echo112 bind testEP {
}

@http:ServiceConfig {
    basePath:"hello/"
}
service<http:Service> serviceHello bind testEP {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/test/"
    }
     productsInfo (endpoint client, http:Request req) {
        http:Response res = new;
        json responseJson = {"echo":"sanitized"};
        res.setJsonPayload(responseJson);
        _ = client -> respond(res);
    }
}

@http:ServiceConfig {
    basePath:"/ech[o"
}
service<http:Service> echo113 bind testEP {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/ech[o/{foo}"
    }
     productsInfo (endpoint client, http:Request req, string foo) {
        http:Response res = new;
        json responseJson = {"echo113": foo};
        res.setJsonPayload(responseJson);
        _ = client -> respond(res);
    }
}

@http:ServiceConfig {
    basePath:"/ech%5Bo14"
}
service<http:Service> echo114 bind testEP {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/ech%5Bo14/{foo}"
    }
     productsInfo (endpoint client, http:Request req, string foo) {
        http:Response res = new;
        json responseJson = {"echo114": foo};
        res.setJsonPayload(responseJson);
        _ = client -> respond(res);
    }
}
