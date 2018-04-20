import ballerina/mime;
import ballerina/http;
import ballerina/io;

endpoint http:NonListener mockEP {
    port:9090
};

@http:ServiceConfig {basePath:"/test"}
service<http:Service> echo bind mockEP {
    @http:ResourceConfig {
        methods:["POST"],
        path:"/largepayload"
    }
    getPayloadFromFileChannel (endpoint client, http:Request request) {
        http:Response response = new;
        mime:Entity responseEntity = new;
        match request.getByteChannel() {
            http:PayloadError err => io:print("invalid value");
            io:ByteChannel byteChannel => responseEntity.setByteChannel(byteChannel);
        }
        response.setEntity(responseEntity);
        _ = client -> respond(response);
    }
}
