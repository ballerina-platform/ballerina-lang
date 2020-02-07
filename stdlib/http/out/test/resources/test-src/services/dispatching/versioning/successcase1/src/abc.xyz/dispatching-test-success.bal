import ballerina/http;

listener http:MockListener passthruEP  = new(9090);

@http:ServiceConfig {
    basePath:"/hello1/{version}",
    versioning:{
       pattern:"v{Major}.{Minor}",
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

@http:ServiceConfig {
    basePath:"/{version}/bar",
    versioning:{
        pattern:"{major}.{minor}"
    }
}
service hello2 on passthruEP {

    @http:ResourceConfig {
        path:"/go"
    }
    resource function sample (http:Caller caller, http:Request req) {
        http:Response res = new;
        res.setJsonPayload({hello:"Only template"});
        checkpanic caller->respond(res);
    }
}

@http:ServiceConfig {
    basePath:"/hello3/{version}/bar",
    versioning:{
        allowNoVersion:true
    }
}
service hello3 on passthruEP {

    @http:ResourceConfig {
        path:"/go"
    }
    resource function sample(http:Caller caller, http:Request req) {
        http:Response res = new;
        res.setJsonPayload({hello:"only allow no version"});
        checkpanic caller->respond(res);
    }
}

@http:ServiceConfig {
    basePath:"/hello4/{version}/bar",
    versioning:{
        matchMajorVersion:true
    }
}
service hello4 on passthruEP {

    @http:ResourceConfig {
        path:"/go"
    }
    resource function sample(http:Caller caller, http:Request req) {
        http:Response res = new;
        res.setJsonPayload({hello:"only match major"});
        checkpanic caller->respond(res);
    }
}

@http:ServiceConfig {
    basePath:"/hello5/bar",
    versioning:{
        pattern:"{MAJOR}.{minor}"
    }
}
service hello5 on passthruEP {

    @http:ResourceConfig {
        path:"/go"
    }
    resource function sample(http:Caller caller, http:Request req) {
        http:Response res = new;
        res.setJsonPayload({hello:"without version segment in basePath"});
        checkpanic caller->respond(res);
    }
}
