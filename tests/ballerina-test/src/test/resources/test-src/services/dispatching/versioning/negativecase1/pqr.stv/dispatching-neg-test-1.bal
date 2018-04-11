package pqr.stv;

import ballerina/http;
import ballerina/io;

endpoint http:NonListener passthruEP {
    port:9090
};

@http:ServiceConfig {
    basePath:"/hello1/{version}",
    versioning:{
       pattern:"v{ajor}.{min}",
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
