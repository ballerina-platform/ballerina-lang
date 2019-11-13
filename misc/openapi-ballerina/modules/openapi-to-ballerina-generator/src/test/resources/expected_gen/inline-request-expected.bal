import ballerina/http;
import ballerina/openapi;

listener http:Listener ep0 = new(9090);

@openapi:ServiceInfo {
    contract: "/var/folders/mz/xmjfm34s1n99v74_jtsbsdcw0000gn/T/openapi-cmd5783925196792783355/src/inlineModule/resources/inline-request-body.yaml"
}
@http:ServiceConfig {
    basePath: "/"
}
service inlineservice on ep0 {

    @http:ResourceConfig {
        methods:["POST"],
        path:"/user", 
        body:"body"
    }
    resource function addUser(http:Caller caller, http:Request req, record { string userName; string userPhone;  } body ) returns error? {

    }

}