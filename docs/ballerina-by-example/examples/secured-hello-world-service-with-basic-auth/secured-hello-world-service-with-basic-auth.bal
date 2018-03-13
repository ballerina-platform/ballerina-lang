import ballerina.net.http;
import ballerina.auth.authz;
import ballerina.auth.basic;

// The secure service uses a HTTPS endpoint, exposed via the port 9096.
endpoint<http:Service> backendEp {
    port:9096,
    ssl:{
        keyStoreFile:"${ballerina.home}/bre/security/ballerinaKeystore.p12",
        keyStorePassword:"ballerina",
        certPassword:"ballerina"
    }
}

// Sample Hello Service, which require authentication and authorization.
// This service is registered against the previously defined endpoint 'backendEp'.
@http:serviceConfig {
    basePath:"/helloWorld",
    endpoints:[backendEp]
}
service<http:Service> helloWorld {
    @http:resourceConfig {
        methods:["GET"],
        path:"/sayHello"
    }
    resource sayHello (http:ServerConnector conn, http:Request request) {
        http:Response res = {};
        // Perform the authentication and authorization check. This is done by calling the 'checkAuth' fucntion
        // defined below.
        AuthStatus authStatus = checkAuth(request, "scope2", "sayHello");
        if(authStatus.success) {
            // The request has been authenticated and authorized successfully.
            res.setJsonPayload("Hello, World!!");
        } else {
            // The request has failed the auth check, set the status code and the reason phrase appropriately. 
            res = {statusCode:authStatus.statusCode, reasonPhrase:authStatus.message};
        }
        _ = conn -> respond(res);
    }
}

// Helper function which performs authentication and authorization checks.
function checkAuth (http:Request request, string scopeName, string resourceName) (AuthStatus) {
    //The Basic Authentication handler and Authorization Handler is used for authentication and authorization.
    // First, the authentication check is done, which is then followed by the authorization check.
    basic:HttpBasicAuthnHandler authnHandler = {};
    authz:HttpAuthzHandler authzHandler = {};
    AuthStatus status;
    // First, the authentication check is performed.
    if (!authnHandler.handle(request)) {
        // If authentication fails, the status code is set as 401.
        status = {success:false, statusCode:401, message:"Unauthenticated"};
        // If authentication is successful, an optional authorization step can be performed.
        // Here, to access the resource 'sayHello' , user needs to be in the same groups
        // which are mapped to 'scope2'.
    } else if (!authzHandler.handle(request, scopeName, resourceName)) {
        // If authorization check fails, the status code is set as 403.
        status = {success:false, statusCode:403, message:"Unauthorized"};
    } else {
        status = {success:true, statusCode:200, message:"Successful"};
    }
    return status;
}

// Represents the status of the auth check.
public struct AuthStatus {
    boolean success;
    int statusCode;
    string message;
}
