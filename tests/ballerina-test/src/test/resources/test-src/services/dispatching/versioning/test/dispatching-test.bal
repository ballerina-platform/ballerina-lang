package versioning.test;

import ballerina/http;
import ballerina/io;

endpoint http:NonListener passthruEP {
    port:9090
};

@http:ServiceConfig {
    basePath:"/hello1/{version}",
    versioning:{
       pattern:"v{major}.{minor}",
       allowNoVersion:true,
       matchMajorVersion:true
    }
}
service<http:Service> hello1 bind passthruEP {

    @http:ResourceConfig {
        path:"/go"
    }
    sample (endpoint conn, http:Request req) {
        http:Response res = new;
        res.setJsonPayload({hello:"common service"});
        _ = conn -> respond(res);
    }
}

@http:ServiceConfig {
    basePath:"/{version}/bar",
    versioning:{
        pattern:"{major}.{minor}"
    }
}
service<http:Service> hello2 bind passthruEP {

    @http:ResourceConfig {
        path:"/go"
    }
    sample (endpoint conn, http:Request req) {
        http:Response res = new;
        res.setJsonPayload({hello:"Only template"});
        _ = conn -> respond(res);
    }
}

@http:ServiceConfig {
    basePath:"/hello3/{version}/bar",
    versioning:{
        allowNoVersion:true
    }
}
service<http:Service> hello3 bind passthruEP {

    @http:ResourceConfig {
        path:"/go"
    }
    sample (endpoint conn, http:Request req) {
        http:Response res = new;
        res.setJsonPayload({hello:"only allow no version"});
        _ = conn -> respond(res);
    }
}

@http:ServiceConfig {
    basePath:"/hello4/{version}/bar",
    versioning:{
        matchMajorVersion:true
    }
}
service<http:Service> hello4 bind passthruEP {

    @http:ResourceConfig {
        path:"/go"
    }
    sample (endpoint conn, http:Request req) {
        http:Response res = new;
        res.setJsonPayload({hello:"only match major"});
        _ = conn -> respond(res);
    }
}

@http:ServiceConfig {
    basePath:"/hello5/bar",
    versioning:{
        pattern:"{major}.{minor}"
    }
}
service<http:Service> hello5 bind passthruEP {

    @http:ResourceConfig {
        path:"/go"
    }
    sample (endpoint conn, http:Request req) {
        http:Response res = new;
        res.setJsonPayload({hello:"without version segment in basePath"});
        _ = conn -> respond(res);
    }
}
