import ballerina/io;
import ballerina/net.http;
import ballerina/net.http.mock;

endpoint<mock:NonListeningService> testEP {
    port:9090
}

@http:serviceConfig {
    basePath:"/ecommerceservice",
    endpoints:[testEP]
}
service<http:Service> Ecommerce {
    @http:resourceConfig {
        methods:["GET"],
        path:"/products/{productId}/{regId}"
    }
    resource productsInfo1 (http:ServerConnector conn, http:Request req, string productId, string regId) {
        string orderId = req.getHeader("X-ORDER-ID");
        io:println("Order ID " + orderId);
        io:println("Product ID " + productId);
        io:println("Reg ID " + regId);
        json responseJson = {"X-ORDER-ID":orderId, "ProductID":productId, "RegID":regId};
        io:println(responseJson.toString());

        http:Response res = {};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/products2/{productId}/{regId}/item"
    }
    resource productsInfo2 (http:ServerConnector conn, http:Request req, string productId, string regId) {
        json responseJson;
        io:println("Product ID " + productId);
        io:println("Reg ID " + regId);
        responseJson = {"Template":"T2", "ProductID":productId, "RegID":regId};
        io:println(responseJson.toString());

        http:Response res = {};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/products3/{productId}/{regId}/*"
    }
    resource productsInfo3 (http:ServerConnector conn, http:Request req, string productId, string regId) {
        json responseJson;
        io:println("Product ID " + productId);
        io:println("Reg ID " + regId);
        responseJson = {"Template":"T3", "ProductID":productId, "RegID":regId};
        io:println(responseJson.toString());

        http:Response res = {};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/products/{productId}"
    }
    resource productsInfo4 (http:ServerConnector conn, http:Request req, string productId) {
        json responseJson;
        map params = req.getQueryParams();
        string rID;
        rID, _ = (string)params.regID;
        io:println("Product ID " + productId);
        io:println("Reg ID " + rID);
        responseJson = {"Template":"T4", "ProductID":productId, "RegID":rID};
        io:println(responseJson.toString());

        http:Response res = {};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/products"
    }
    resource productsInfo6 (http:ServerConnector conn, http:Request req) {
        json responseJson;
        map params = req.getQueryParams();
        string prdID;
        string rID;
        prdID, _ = (string)params.prodID;
        rID, _ = (string)params.regID;
        io:println ("Product ID " + prdID);
        io:println ("Reg ID " + rID);
        responseJson = {"Template":"T6", "ProductID":prdID, "RegID":rID};
        io:println (responseJson.toString ());

        http:Response res = {};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/products5/{productId}/reg"
    }
    resource productsInfo5 (http:ServerConnector conn, http:Request req, string productId) {
        json responseJson;
        map params = req.getQueryParams();
        string rID;
        rID, _ = (string)params.regID;
        io:println("Product ID " + productId);
        io:println("Reg ID " + rID);
        responseJson = {"Template":"T5", "ProductID":productId, "RegID":rID};
        io:println(responseJson.toString());

        http:Response res = {};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        path:""
    }
    resource echo1 (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"echo11":"echo11"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }
}

@http:serviceConfig {
    basePath:"/options",
    endpoints:[testEP]
}
service<http:Service> echo111 {

    @http:resourceConfig {
        methods:["POST", "UPDATE"],
        path : "/test"
    }
    resource productsInfo99 (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        methods:["OPTIONS"],
        path : "/hi"
    }
    resource productsOptions (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"echo":"wso2"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        methods:["GET", "PUT"],
        path : "/test"
    }
    resource productsInfo98 (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        _ = conn -> respond(res);

    }

    @http:resourceConfig {
        methods:["GET"],
        path : "/getme"
    }
    resource productsGet (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"echo":"get"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        methods:["POST"],
        path : "/post"
    }
    resource productsPOST (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"echo":"post"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        methods:["PUT"],
        path : "/put"
    }
    resource productsPUT (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"echo":"put"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }
}

@http:serviceConfig {
    basePath:"/noResource",
    endpoints:[testEP]
}
service<http:Service> echo112 {
}

@http:serviceConfig {
    basePath:"hello/",
    endpoints:[testEP]
}
service<http:Service> serviceHello {

    @http:resourceConfig {
        methods:["GET"],
        path:"/test/"
    }
    resource productsInfo (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"echo":"sanitized"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }
}

@http:serviceConfig {
    basePath:"/ech[o",
    endpoints:[testEP]
}
service<http:Service> echo113 {

    @http:resourceConfig {
        methods:["GET"],
        path:"/ech[o/{foo}"
    }
    resource productsInfo (http:ServerConnector conn, http:Request req, string foo) {
        http:Response res = {};
        json responseJson = {"echo113": foo};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }
}

@http:serviceConfig {
    basePath:"/ech%5Bo14",
    endpoints:[testEP]
}
service<http:Service> echo114 {

    @http:resourceConfig {
        methods:["GET"],
        path:"/ech%5Bo14/{foo}"
    }
    resource productsInfo (http:ServerConnector conn, http:Request req, string foo) {
        http:Response res = {};
        json responseJson = {"echo114": foo};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }
}
