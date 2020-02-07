import ballerina/http;

listener http:MockListener mockEP  = new(9090);

@http:ServiceConfig {basePath : "/autoCompress", compression: {enable: http:COMPRESSION_AUTO}}
service autoCompress on mockEP {
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

@http:ServiceConfig {compression: {contentTypes:["text/plain"]}}
service autoCompressWithContentType on mockEP {
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

@http:ServiceConfig {basePath : "/alwaysCompress", compression: {enable: http:COMPRESSION_ALWAYS}}
service alwaysCompress on mockEP {
     @http:ResourceConfig {
        methods:["GET"],
        path:"/"
        }
    resource function test2 (http:Caller caller, http:Request req) {
        http:Response res = new;
        res.setTextPayload("Hello World!!!");
        checkpanic caller->respond(res);
    }
}

@http:ServiceConfig {compression: {enable: http:COMPRESSION_ALWAYS, contentTypes:["text/plain","Application/Json"]}}
service alwaysCompressWithContentType on mockEP {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource function test2 (http:Caller caller, http:Request req) {
        http:Response res = new;
        res.setJsonPayload({ test: "testValue" }, "application/json");
        checkpanic caller->respond(res);
    }
}

@http:ServiceConfig {basePath : "/neverCompress", compression: {enable: http:COMPRESSION_NEVER}}
service neverCompress on mockEP {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource function test3 (http:Caller caller, http:Request req) {
        http:Response res = new;
        res.setTextPayload("Hello World!!!");
        checkpanic caller->respond(res);
    }
}

@http:ServiceConfig {compression: {enable: http:COMPRESSION_NEVER, contentTypes:["text/plain","application/xml"]}}
service neverCompressWithContentType on mockEP {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource function test3 (http:Caller caller, http:Request req) {
        http:Response res = new;
        res.setTextPayload("Hello World!!!");
        checkpanic caller->respond(res);
    }
}

@http:ServiceConfig {basePath : "/userOverridenValue", compression: {enable: http:COMPRESSION_NEVER}}
service userOverridenValue on mockEP {
    @http:ResourceConfig {
            methods:["GET"],
            path:"/"
    }
    resource function test3 (http:Caller caller, http:Request req) {
        http:Response res = new;
        res.setTextPayload("Hello World!!!");
        res.setHeader("content-encoding", "deflate");
        checkpanic caller->respond(res);
    }
}

@http:ServiceConfig {compression: {contentTypes:["text/plain"]}}
service autoCompressWithInCompatibleContentType on mockEP {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource function test1 (http:Caller caller, http:Request req) {
        http:Response res = new;
        res.setJsonPayload({ test: "testValue" }, "application/json");
        checkpanic caller->respond(res);
    }
}

@http:ServiceConfig {compression: {enable: http:COMPRESSION_ALWAYS, contentTypes:[]}}
service alwaysCompressWithEmptyContentType on mockEP {
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
