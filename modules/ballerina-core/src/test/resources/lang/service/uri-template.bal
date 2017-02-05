import ballerina.lang.message;
import ballerina.lang.system;
import ballerina.lang.string;
import ballerina.lang.json;

@BasePath ("/ecommerceservice")
service Ecommerce {
    @GET
    @Path ("/products/{productId}/{regId}")
    resource productsInfo1 (message m, @PathParam ("productId") string prdID, @PathParam ("regId") string rID) {
        message response;
        string orderId;
        json responseJson;
        response = new message ();

        orderId = message:getHeader(m, "X-ORDER-ID");
        system:println("Order ID " + orderId);
        system:println("Product ID " + prdID);
        system:println("Reg ID " + rID);
        responseJson = `{"X-ORDER-ID":${orderId}, "ProductID":${prdID}, "RegID":${rID}}`;
        system:println(json:toString(responseJson));
        message:setJsonPayload(response, responseJson);
        reply response;
    }

    @GET
    @Path ("/products2/{productId}/{regId}/item")
    resource productsInfo2 (message m, @PathParam ("productId") string prdID, @PathParam ("regId") string rID) {
        message response;
        json responseJson;
        response = new message ();
        system:println("Product ID " + prdID);
        system:println("Reg ID " + rID);
        responseJson = `{"Template":"T2", "ProductID":${prdID}, "RegID":${rID}}`;
        system:println(json:toString(responseJson));
        message:setJsonPayload(response, responseJson);
        reply response;
    }

    @GET
    @Path ("/products3/{productId}/{regId}/*")
    resource productsInfo3 (message m, @PathParam ("productId") string prdID, @PathParam ("regId") string rID) {
        message response;
        json responseJson;
        response = new message ();
        system:println("Product ID " + prdID);
        system:println("Reg ID " + rID);
        responseJson = `{"Template":"T3", "ProductID":${prdID}, "RegID":${rID}}`;
        system:println(json:toString(responseJson));
        message:setJsonPayload(response, responseJson);
        reply response;
    }

    @GET
    @Path ("/products/{productId}?regID={regID}")
    resource productsInfo4 (message m, @PathParam ("productId") string prdID, @QueryParam ("regID") string rID) {
        message response;
        json responseJson;
        response = new message ();
        system:println("Product ID " + prdID);
        system:println("Reg ID " + rID);
        responseJson = `{"Template":"T4", "ProductID":${prdID}, "RegID":${rID}}`;
        system:println(json:toString(responseJson));
        message:setJsonPayload(response, responseJson);
        reply response;
    }

    @GET
    @Path ("/products5/{productId}/reg?regID={regID}*")
    resource productsInfo5 (message m, @PathParam ("productId") string prdID, @QueryParam ("regID") string rID) {
        message response;
        json responseJson;
        response = new message ();
        system:println("Product ID " + prdID);
        system:println("Reg ID " + rID);
        responseJson = `{"Template":"T5", "ProductID":${prdID}, "RegID":${rID}}`;
        system:println(json:toString(responseJson));
        message:setJsonPayload(response, responseJson);
        reply response;
    }
}