import ballerina/http;
import ballerina/io;

endpoint http:Listener echoEP {
    port:9099
};

@http:ServiceConfig {
    basePath:"/echo"
}
service<http:Service> echo bind echoEP{

    @http:ResourceConfig {
        methods:["POST"],
        path:"/"
    }
    echo (endpoint caller, http:Request req) {
        http:Response resp = new;
        var result = req.getTextPayload();
        match result {
            error payloadError => io:println(payloadError.message);
            string payload => {
                resp.setTextPayload(payload);
                _ = caller -> respond(resp);
            }
        }
    }
}
