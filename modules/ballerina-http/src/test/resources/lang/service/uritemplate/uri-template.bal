import ballerina.lang.messages;
import ballerina.lang.system;
import ballerina.lang.jsons;
import ballerina.net.http;

@http:configuration {basePath:"/ecommerceservice"}
service<http> Ecommerce {
    @http:resourceConfig {
        methods:["GET"],
        path:"/products/{productId}/{regId}"
    }
    resource productsInfo1 (message m, @http:PathParam {value:"productId"} string prdID, @http:PathParam {value:"regId"} string rID) {
        string orderId;
        json responseJson;
        message response = {};

        orderId = messages:getHeader(m, "X-ORDER-ID");
        system:println("Order ID " + orderId);
        system:println("Product ID " + prdID);
        system:println("Reg ID " + rID);
        responseJson = {"X-ORDER-ID":orderId, "ProductID":prdID, "RegID":rID};
        system:println(jsons:toString(responseJson));
        messages:setJsonPayload(response, responseJson);
        reply response;
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/products2/{productId}/{regId}/item"
    }
    resource productsInfo2 (message m, @http:PathParam {value:"productId"} string prdID, @http:PathParam {value:"regId"} string rID) {
        json responseJson;
        message response = {};
        system:println("Product ID " + prdID);
        system:println("Reg ID " + rID);
        responseJson = {"Template":"T2", "ProductID":prdID, "RegID":rID};
        system:println(jsons:toString(responseJson));
        messages:setJsonPayload(response, responseJson);
        reply response;
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/products3/{productId}/{regId}/*"
    }
    resource productsInfo3 (message m, @http:PathParam {value:"productId"} string prdID, @http:PathParam {value:"regId"} string rID) {
        json responseJson;
        message response = {};
        system:println("Product ID " + prdID);
        system:println("Reg ID " + rID);
        responseJson = {"Template":"T3", "ProductID":prdID, "RegID":rID};
        system:println(jsons:toString(responseJson));
        messages:setJsonPayload(response, responseJson);
        reply response;
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/products/{productId}"
    }
    resource productsInfo4 (message m, @http:PathParam {value:"productId"} string prdID, @http:QueryParam {value:"regID"} string rID) {
        json responseJson;
        message response = {};
        system:println("Product ID " + prdID);
        system:println("Reg ID " + rID);
        responseJson = {"Template":"T4", "ProductID":prdID, "RegID":rID};
        system:println(jsons:toString(responseJson));
        messages:setJsonPayload(response, responseJson);
        reply response;
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/products"
    }
    resource productsInfo6 (message m, @http:QueryParam {value:"prodId"} string prdID, @http:QueryParam {value:"regID"} string rID) {
        json responseJson;
        message response = {};
        system:println ("Product ID " + prdID);
        system:println ("Reg ID " + rID);
        responseJson = {"Template":"T6", "ProductID":prdID, "RegID":rID};
        system:println (jsons:toString (responseJson));
        messages:setJsonPayload (response, responseJson);
        reply response;
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/products5/{productId}/reg"
    }
    resource productsInfo5 (message m, @http:PathParam {value:"productId"} string prdID, @http:QueryParam {value:"regID"} string rID) {
        json responseJson;
        message response = {};
        system:println("Product ID " + prdID);
        system:println("Reg ID " + rID);
        responseJson = {"Template":"T5", "ProductID":prdID, "RegID":rID};
        system:println(jsons:toString(responseJson));
        messages:setJsonPayload(response, responseJson);
        reply response;
    }

    @http:resourceConfig {
        path:""
    }
    resource echo1 (message m, string foo) {
        message response = {};
        json responseJson = {"echo11":"echo11"};
        messages:setJsonPayload(response, responseJson);
        reply response;
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
    resource productsInfo99 (message m) {
        message response = {};
        reply response;
    }

    @http:resourceConfig {
        methods:["OPTIONS"],
        path : "/hi"
    }
    resource productsOptions (message m) {
        message response = {};
        json responseJson = {"echo":"wso2"};
        messages:setJsonPayload(response, responseJson);
        reply response;
    }

    @http:resourceConfig {
        methods:["GET", "PUT"],
        path : "/test"
    }
    resource productsInfo98 (message m) {
        message response = {};
        reply response;

    }

    @http:resourceConfig {
        methods:["GET"],
        path : "/getme"
    }
    resource productsGet (message m) {
        message response = {};
        json responseJson = {"echo":"get"};
        messages:setJsonPayload(response, responseJson);
        reply response;
    }

    @http:resourceConfig {
        methods:["POST"],
        path : "/post"
    }
    resource productsPOST (message m) {
        message response = {};
        json responseJson = {"echo":"post"};
        messages:setJsonPayload(response, responseJson);
        reply response;
    }

    @http:resourceConfig {
        methods:["PUT"],
        path : "/put"
    }
    resource productsPUT (message m) {
        message response = {};
        json responseJson = {"echo":"put"};
        messages:setJsonPayload(response, responseJson);
        reply response;
    }
}

@http:configuration {
    basePath:"/noResource"
}
service<http> echo112 {
}