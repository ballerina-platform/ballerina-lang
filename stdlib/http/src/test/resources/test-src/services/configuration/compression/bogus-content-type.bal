import ballerina/http;

listener http:MockListener mockEP  = new(9091);

@http:ServiceConfig {compression: {enable: http:COMPRESSION_ALWAYS, contentTypes:["hello=/#bal", "fywvwiuwi"]}}
service alwaysCompressWithBogusContentType on mockEP {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource function test1 (http:Caller caller, http:Request req) {
        http:Response res = new;
        res.setTextPayload("Hello World!!!");
        checkpanic caller->respond(res);
    }
}
