import ballerina/http;
import ballerina/mime;

@Description {value:"Define the attributes associated with a service endpoint."}
endpoint http:ServiceEndpoint echoEP {
    port:9090
};

@Description {value:"The BasePath attribute associates a path to the service."}
@Description {value:"It binds the service endpoint to the service."}
@http:ServiceConfig { basePath:"/foo" }
service<http:Service> echo bind echoEP {
    @Description {value:"The POST annotation restricts the resource to accept POST requests only. Similarly, there are different annotations for each HTTP verb."}
    @Description {value:"The Path attribute associates a subpath to the resource."}
    @http:ResourceConfig {
        methods:["POST"],
        path:"/bar"
    }
    echo (endpoint conn, http:Request req) {
        // This method gets the request payload.
        var result = req.getJsonPayload();
        http:Response res = {};
        match result {
            http:PayloadError err => {
                res = {statusCode:500};
                res.setStringPayload(err.message);
            }
            json value =>{
                res.setJsonPayload(value);
            }
        }
        // Reply to the client with the response.
        _ = conn -> respond(res);
    }
}
