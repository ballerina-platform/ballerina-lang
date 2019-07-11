import ballerina/mime;
import ballerina/http;
import ballerina/io;

listener http:MockListener mockEP = new(9090);

@http:ServiceConfig {basePath:"/test"}
service echo on mockEP {
    @http:ResourceConfig {
        methods:["POST"],
        path:"/largepayload"
    }
    resource function getPayloadFromFileChannel(http:Caller caller, http:Request request) {
        http:Response response = new;
        mime:Entity responseEntity = new;

        var result = request.getByteChannel();
        if (result is io:ReadableByteChannel) {
            responseEntity.setByteChannel(result);
        } else {
            io:print("Error in getting byte channel");
        }

        response.setEntity(responseEntity);
        checkpanic caller->respond(response);
    }
}

function testHeaderWithRequest() returns @tainted string {
    http:Request request = new;
    mime:Entity entity = new;
    entity.setHeader("123Authorization", "123Basicxxxxxx");
    request.setEntity(entity);
    return (request.getHeader("123Authorization"));
}

function testHeaderWithResponse() returns @tainted string {
    http:Response response = new;
    mime:Entity entity = new;
    entity.setHeader("123Authorization", "123Basicxxxxxx");
    response.setEntity(entity);
    return (response.getHeader("123Authorization"));
}
