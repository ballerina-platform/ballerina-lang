import ballerina/http;


listener http:MockListener passthruEP  = new(9090);

@http:ServiceConfig {
    basePath:"/hello1/{version}",
    versioning:{
       pattern:"v{ajor}.{min}",
       allowNoVersion:true,
       matchMajorVersion:true
    }
}
service hello1 on passthruEP {

    @http:ResourceConfig {
        path:"/go"
    }
    resource function sample(http:Caller caller, http:Request req) {
        http:Response res = new;
        res.setJsonPayload({hello:"common service"});
        checkpanic caller->respond(res);
    }
}
