import ballerina.io;
import ballerina.net.http;

@http:configuration {basePath:"/ecommerceservice"}
service<http> Ecommerce {
    @http:resourceConfig {
        methods:["GET"],
        path:"/products/{productId}/{regId}"
    }
    resource productsInfo1 (http:Connection conn, http:InRequest req, string productId, string regId) {
        string orderId = req.getHeader("X-ORDER-ID");
        io:println("Order ID " + orderId);
        io:println("Product ID " + productId);
        io:println("Reg ID " + regId);
        json responseJson = {"X-ORDER-ID":orderId, "ProductID":productId, "RegID":regId};
        io:println(responseJson.toString());

        http:OutResponse res = {};
        res.setJsonPayload(responseJson);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/products2/{productId}/{regId}/item"
    }
    resource productsInfo2 (http:Connection conn, http:InRequest req, string productId, string regId) {
        json responseJson;
        io:println("Product ID " + productId);
        io:println("Reg ID " + regId);
        responseJson = {"Template":"T2", "ProductID":productId, "RegID":regId};
        io:println(responseJson.toString());

        http:OutResponse res = {};
        res.setJsonPayload(responseJson);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/products3/{productId}/{regId}/*"
    }
    resource productsInfo3 (http:Connection conn, http:InRequest req, string productId, string regId) {
        json responseJson;
        io:println("Product ID " + productId);
        io:println("Reg ID " + regId);
        responseJson = {"Template":"T3", "ProductID":productId, "RegID":regId};
        io:println(responseJson.toString());

        http:OutResponse res = {};
        res.setJsonPayload(responseJson);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/products/{productId}"
    }
    resource productsInfo4 (http:Connection conn, http:InRequest req, string productId) {
        json responseJson;
        map params = req.getQueryParams();
        string rID;
        rID, _ = (string)params.regID;
        io:println("Product ID " + productId);
        io:println("Reg ID " + rID);
        responseJson = {"Template":"T4", "ProductID":productId, "RegID":rID};
        io:println(responseJson.toString());

        http:OutResponse res = {};
        res.setJsonPayload(responseJson);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/products"
    }
    resource productsInfo6 (http:Connection conn, http:InRequest req) {
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

        http:OutResponse res = {};
        res.setJsonPayload(responseJson);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/products5/{productId}/reg"
    }
    resource productsInfo5 (http:Connection conn, http:InRequest req, string productId) {
        json responseJson;
        map params = req.getQueryParams();
        string rID;
        rID, _ = (string)params.regID;
        io:println("Product ID " + productId);
        io:println("Reg ID " + rID);
        responseJson = {"Template":"T5", "ProductID":productId, "RegID":rID};
        io:println(responseJson.toString());

        http:OutResponse res = {};
        res.setJsonPayload(responseJson);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:""
    }
    resource echo1 (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        json responseJson = {"echo11":"echo11"};
        res.setJsonPayload(responseJson);
        _ = conn.respond(res);
    }
}

@http:configuration {
    basePath:"/options"
}
service<http> echo111 {

    @http:resourceConfig {
        methods:["POST", "UPDATE"],
        path : "/test"
    }
    resource productsInfo99 (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["OPTIONS"],
        path : "/hi"
    }
    resource productsOptions (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        json responseJson = {"echo":"wso2"};
        res.setJsonPayload(responseJson);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["GET", "PUT"],
        path : "/test"
    }
    resource productsInfo98 (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        _ = conn.respond(res);

    }

    @http:resourceConfig {
        methods:["GET"],
        path : "/getme"
    }
    resource productsGet (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        json responseJson = {"echo":"get"};
        res.setJsonPayload(responseJson);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["POST"],
        path : "/post"
    }
    resource productsPOST (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        json responseJson = {"echo":"post"};
        res.setJsonPayload(responseJson);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["PUT"],
        path : "/put"
    }
    resource productsPUT (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        json responseJson = {"echo":"put"};
        res.setJsonPayload(responseJson);
        _ = conn.respond(res);
    }
}

@http:configuration {
    basePath:"/noResource"
}
service<http> echo112 {
}

@http:configuration {
    basePath:"hello/"
}
service<http> serviceHello {

    @http:resourceConfig {
        methods:["GET"],
        path:"/test/"
    }
    resource productsInfo (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        json responseJson = {"echo":"sanitized"};
        res.setJsonPayload(responseJson);
        _ = conn.respond(res);
    }
}

@http:configuration {
    basePath:"ech[o"
}
service<http> echo113 {

    @http:resourceConfig {
        methods:["GET"],
        path:"/ech[o/{foo}"
    }
    resource productsInfo (http:Connection conn, http:InRequest req, string foo) {
        http:OutResponse res = {};
        json responseJson = {"echo113": foo};
        res.setJsonPayload(responseJson);
        _ = conn.respond(res);
    }
}

@http:configuration {
    basePath:"ech%5Bo14"
}
service<http> echo114 {

    @http:resourceConfig {
        methods:["GET"],
        path:"/ech%5Bo14/{foo}"
    }
    resource productsInfo (http:Connection conn, http:InRequest req, string foo) {
        http:OutResponse res = {};
        json responseJson = {"echo114": foo};
        res.setJsonPayload(responseJson);
        _ = conn.respond(res);
    }
}
