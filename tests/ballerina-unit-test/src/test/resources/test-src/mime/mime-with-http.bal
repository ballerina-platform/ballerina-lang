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
        _ = caller -> respond(response);
    }
}
