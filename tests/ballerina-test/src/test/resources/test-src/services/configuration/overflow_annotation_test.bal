import ballerina/http;

endpoint http:NonListener mockEP {
    port:9090
};

@http:ServiceConfig {basePath : "/defaultOverflow"}
service<http:Service> defaultOverflow bind mockEP {
    @http:ResourceConfig {
        methods:["POST"],
        path:"/"
    }
    test1 (endpoint caller, http:Request req) {
        http:Response response = new;
        mime:Entity responseEntity = new;
        match request.getByteChannel() {
            error err => io:print(err.message);
            io:ByteChannel byteChannel => responseEntity.setByteChannel(byteChannel);
        }
        response.setEntity(responseEntity);
        _ = caller -> respond(response);
    }
}
