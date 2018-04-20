
import ballerina/http;
import ballerina/io;

endpoint http:NonListener passthruEP {
    port:9090
};

@http:ServiceConfig {
    basePath:"/echo/{version}",
    versioning:{
       pattern:"v{Major}.{Minor}",
       allowNoVersion:true,
       matchMajorVersion:true
    }
}
service<http:Service> echo bind passthruEP {

    @http:ResourceConfig {
        path:"/go"
    }
    sample (endpoint conn, http:Request req) {
        http:Response res = new;
        res.setJsonPayload({hello:"common service"});
        _ = conn -> respond(res);
    }
}
