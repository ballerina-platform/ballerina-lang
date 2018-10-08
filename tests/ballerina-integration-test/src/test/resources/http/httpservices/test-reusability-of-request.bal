import ballerina/http;
import ballerina/io;
import ballerina/file;
import ballerina/mime;

endpoint http:Client clientEP1 {
    url:"http://localhost:9115/test"
};

endpoint http:Listener testEP {
    port:9115
};

@http:ServiceConfig {basePath:"/reuseObj"}
service<http:Service> testService_1 bind testEP {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/request_without_entity"
    }
    getWithoutEntity(endpoint outboundEP, http:Request clientRequest) {
        http:Request clientReq = new;
        http:Response firstResponse = check clientEP1 -> get("", message = clientReq);
        http:Response secondResponse = check clientEP1 -> get("", message = clientReq);
        http:Response testResponse = new;
        string firstVal = untaint check firstResponse.getTextPayload();
        string secondVal = untaint check secondResponse.getTextPayload();
        testResponse.setTextPayload(firstVal + secondVal);
        _ = outboundEP -> respond(testResponse);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/request_with_empty_entity"
    }
    getWithEmptyEntity(endpoint outboundEP, http:Request clientRequest) {
        http:Request clientReq = new;
        mime:Entity entity = new;
        clientReq.setEntity(entity);
        http:Response firstResponse = check clientEP1 -> get("", message = clientReq);
        http:Response secondResponse = check clientEP1 -> get("", message = clientReq);
        http:Response testResponse = new;
        string firstVal = untaint check firstResponse.getTextPayload();
        string secondVal = untaint check secondResponse.getTextPayload();
        testResponse.setTextPayload(firstVal + secondVal);
        _ = outboundEP -> respond(testResponse);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/two_request_same_entity"
    }
    getWithEntity(endpoint outboundEP, http:Request clientRequest) {
        http:Request clientReq = new;
        clientReq.setHeader("test1", "value1");
        http:Request newRequest = new;
        mime:Entity entity = check clientReq.getEntity();
        newRequest.setEntity(entity);
        http:Response firstResponse = check clientEP1 -> get("", message = clientReq);
        newRequest.setHeader("test2", "value2");
        http:Response secondResponse = check clientEP1 -> get("", message = newRequest);
        http:Response testResponse = new;
        string firstVal = untaint check firstResponse.getTextPayload();
        string secondVal = untaint check secondResponse.getTextPayload();
        testResponse.setTextPayload(firstVal + secondVal);
        _ = outboundEP -> respond(testResponse);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/request_with_datasource"
    }
    postWithEntity(endpoint outboundEP, http:Request clientRequest) {
        http:Request clientReq = new;
        clientReq.setTextPayload("String datasource");
        http:Response firstResponse = check clientEP1 -> post("/datasource", clientReq);
        http:Response secondResponse = check clientEP1 -> post("/datasource", clientReq);
        http:Response testResponse = new;
        string firstVal = untaint check firstResponse.getTextPayload();
        string secondVal = untaint check secondResponse.getTextPayload();
        testResponse.setTextPayload(firstVal + secondVal);
        _ = outboundEP -> respond(testResponse);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/request_with_bytechannel"
    }
    postWithByteChannel(endpoint outboundEP, http:Request clientRequest) {
        http:Request clientReq = new;
        io:ReadableByteChannel byteChannel = check clientRequest.getByteChannel();
        clientReq.setByteChannel(byteChannel, contentType = "text/plain");
        http:Response firstResponse = check clientEP1 -> post("/consumeChannel", clientReq);
        var secondResponse = clientEP1 -> post("/consumeChannel", clientReq);
        http:Response testResponse = new;
        string secondVal;
        match secondResponse {
            error err => {
                secondVal = err.message;
            }
            http:Response response => {
                secondVal = check response.getTextPayload();
            }
        }
        string firstVal = check firstResponse.getTextPayload();
        testResponse.setTextPayload(untaint firstVal + untaint secondVal);
        _ = outboundEP -> respond(testResponse);
    }
}

@http:ServiceConfig {basePath:"/test"}
service<http:Service> testService_2 bind testEP {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    testForGet(endpoint outboundEP, http:Request clientRequest) {
        http:Response response = new;
        response.setTextPayload("Hello from GET!");
        _ = outboundEP -> respond(response);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/datasource"
    }
    testForPost(endpoint outboundEP, http:Request clientRequest) {
        http:Response response = new;
        response.setTextPayload("Hello from POST!");
        _ = outboundEP -> respond(response);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/consumeChannel"
    }
    testRequestBody(endpoint outboundEP, http:Request clientRequest) {
        http:Response response = new;
        var stringPayload = clientRequest.getTextPayload();
        match stringPayload {
            string receivedVal => {
                response.setTextPayload(untaint receivedVal);
            }
            error err => {
                response.setTextPayload(untaint err.message);
            }
        }
        _ = outboundEP -> respond(response);
    }
}
