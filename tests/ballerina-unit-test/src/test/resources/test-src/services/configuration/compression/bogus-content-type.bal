import ballerina/http;

endpoint http:NonListener mockEP {
    port:9091
};

@http:ServiceConfig {compression: {enable: http:COMPRESSION_ALWAYS, contentTypes:["hello=/#bal", "fywvwiuwi"]}}
service<http:Service> alwaysCompressWithBogusContentType bind mockEP {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    test1 (endpoint conn, http:Request req) {
        http:Response res = new;
        res.setTextPayload("Hello World!!!");
        _ = conn -> respond(res);
    }
}
