import ballerina/lang.messages;
import ballerina/lang.system;
import ballerina/lang.jsons;
import ballerina/http;

@http:configuration {basePath:"/ecommerceservice"}
service<http> Ecommerce {
    @http:GET{}
    @http:Path {value:"/products/{productId}/{regId}"}
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

    @http:GET{}
    @http:Path {value:"/products2/{productId}/{regId}/item"}
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

    @http:GET{}
    @http:Path {value:"/products3/{productId}/{regId}/*"}
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

    @http:GET{}
    @http:Path {value:"/products/{productId}"}
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

    @http:GET{}
    @http:Path {value:"/products"}
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

    @http:GET{}
    @http:Path {value:"/products5/{productId}/reg"}
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

    @http:Path {value:""}
    resource echo1 (message m, string foo) {
        message response = {};
        json responseJson = {"echo11":"echo11"};
        messages:setJsonPayload(response, responseJson);
        reply response;
    }
}