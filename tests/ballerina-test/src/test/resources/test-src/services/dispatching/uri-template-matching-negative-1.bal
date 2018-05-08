import ballerina/http;
import ballerina/http;

endpoint http:NonListener testEP {
    port:9090
};

@http:ServiceConfig {
    basePath:"/hello"
}
service<http:Service> negativeTemplateURI bind testEP {

    @http:ResourceConfig {
        methods:["POST"],
        path:"/echo/{abc}/bar"
    }
     echo1 (endpoint caller, http:Request req, string abc) {
        http:Response res = new;
        json responseJson = {"first":abc, "echo":"echo"};
        res.setJsonPayload(responseJson);
        _ = caller -> respond(res);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/echo/{xyz}/bar"
    }
     echo2 (endpoint caller, http:Request req, string xyz) {
        http:Response res = new;
        json responseJson = {"first":xyz, "echo":"echo"};
        res.setJsonPayload(responseJson);
        _ = caller -> respond(res);
    }
}