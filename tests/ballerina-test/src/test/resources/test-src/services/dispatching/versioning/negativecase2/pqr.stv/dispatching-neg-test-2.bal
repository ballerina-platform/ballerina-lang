package pqr.stv;

import ballerina/http;
import ballerina/io;

endpoint http:NonListener passthruEP {
    port:9090
};

@http:ServiceConfig {
    basePath:"/hello6/{version}/bar",
    versioning:{
        pattern:"v{Minor}",
        matchMajorVersion:true
    }
}
service<http:Service> hello6 bind passthruEP {

    @http:ResourceConfig {
        path:"/go"
    }
    sample (endpoint conn, http:Request req) {
        http:Response res = new;
        res.setJsonPayload({hello:"only match major but no major"});
        _ = conn -> respond(res);
    }
}
