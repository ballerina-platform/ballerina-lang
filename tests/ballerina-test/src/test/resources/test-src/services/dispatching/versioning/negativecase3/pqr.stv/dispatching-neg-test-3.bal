package pqr.stv;

import ballerina/http;
import ballerina/io;

endpoint http:NonListener passthruEP {
    port:9090
};

@http:ServiceConfig {
    basePath:"/echo/{version}/bar",
    versioning:{
        pattern:"v{major}"
    }
}
service<http:Service> echo1 bind passthruEP {

    @http:ResourceConfig {
        path:"/go"
    }
    sample(endpoint conn, http:Request req) {
        http:Response res = new;
        res.setJsonPayload({hello:"only match major but no major"});
        _ = conn -> respond(res);
    }
}

@http:ServiceConfig {
    basePath:"/echo/{version}/bar",
    versioning:{
        pattern:"v{major}.{minor}",
        matchMajorVersion:true
    }
}
service<http:Service> echo2 bind passthruEP {

    @http:ResourceConfig {
        path:"/go"
    }
    sample(endpoint conn, http:Request req) {
        http:Response res = new;
        res.setJsonPayload({hello:"only match major but no major"});
        _ = conn -> respond(res);
    }
}
