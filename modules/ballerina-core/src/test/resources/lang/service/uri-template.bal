import ballerina.lang.messages;
import ballerina.lang.system;
import ballerina.lang.jsons;

@http:BasePath ("/ecommerceservice")
service Ecommerce {
    @http:GET
    @http:Path ("/products/{productId}/{regId}")
    resource productsInfo1 (message m, @http:PathParam ("productId") string prdID, @http:PathParam ("regId") string rID) {
        string orderId;
        json responseJson;
        message response = {};

        orderId = messages:getHeader(m, "X-ORDER-ID");
        system:println("Order ID " + orderId);
        system:println("Product ID " + prdID);
        system:println("Reg ID " + rID);
        responseJson = `{"X-ORDER-ID":${orderId}, "ProductID":${prdID}, "RegID":${rID}}`;
        system:println(jsons:toString(responseJson));
        messages:setJsonPayload(response, responseJson);
        reply response;
    }

    @http:GET
    @http:Path ("/products2/{productId}/{regId}/item")
    resource productsInfo2 (message m, @http:PathParam ("productId") string prdID, @http:PathParam ("regId") string rID) {
        json responseJson;
        message response = {};
        system:println("Product ID " + prdID);
        system:println("Reg ID " + rID);
        responseJson = `{"Template":"T2", "ProductID":${prdID}, "RegID":${rID}}`;
        system:println(jsons:toString(responseJson));
        messages:setJsonPayload(response, responseJson);
        reply response;
    }

    @http:GET
    @http:Path ("/products3/{productId}/{regId}/*")
    resource productsInfo3 (message m, @http:PathParam ("productId") string prdID, @http:PathParam ("regId") string rID) {
        json responseJson;
        message response = {};
        system:println("Product ID " + prdID);
        system:println("Reg ID " + rID);
        responseJson = `{"Template":"T3", "ProductID":${prdID}, "RegID":${rID}}`;
        system:println(jsons:toString(responseJson));
        messages:setJsonPayload(response, responseJson);
        reply response;
    }

    @http:GET
    @http:Path ("/products/{productId}?regID={regID}")
    resource productsInfo4 (message m, @http:PathParam ("productId") string prdID, @http:QueryParam ("regID") string rID) {
        json responseJson;
        message response = {};
        system:println("Product ID " + prdID);
        system:println("Reg ID " + rID);
        responseJson = `{"Template":"T4", "ProductID":${prdID}, "RegID":${rID}}`;
        system:println(jsons:toString(responseJson));
        messages:setJsonPayload(response, responseJson);
        reply response;
    }

    @http:GET
    @http:Path ("/products5/{productId}/reg?regID={regID}*")
    resource productsInfo5 (message m, @http:PathParam ("productId") string prdID, @http:QueryParam ("regID") string rID) {
        json responseJson;
        message response = {};
        system:println("Product ID " + prdID);
        system:println("Reg ID " + rID);
        responseJson = `{"Template":"T5", "ProductID":${prdID}, "RegID":${rID}}`;
        system:println(jsons:toString(responseJson));
        messages:setJsonPayload(response, responseJson);
        reply response;
    }
}