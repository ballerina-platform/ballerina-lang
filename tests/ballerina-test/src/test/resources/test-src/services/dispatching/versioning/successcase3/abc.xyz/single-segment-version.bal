
import ballerina/http;
import ballerina/io;

endpoint http:NonListener passthruEP {
    port:9090
};

@http:ServiceConfig {
    basePath:"/sample/{version}",
    versioning:{
       pattern:"v{Major}.{Minor}",
       allowNoVersion:true,
       matchMajorVersion:true
    }
}
service<http:Service> sample bind passthruEP {

    @http:ResourceConfig {
        path:"/go"
    }
    sample (endpoint conn, http:Request req) {
        http:Response res = new;
        res.setJsonPayload({hello:"common service"});
        _ = conn -> respond(res);
    }
}
