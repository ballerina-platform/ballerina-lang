## Package overview

This package exposes functionality required to handle user authentication and authorization. 

### Authentication
There are two out-of-the-box authentication mechanisms:
- Username and password based authentication
   A user is authenticated based on a username and a password that are stored in a configuration file. By default these are stored in ‘ballerina.conf’. That fille can be changed by a runtime argument.
- JWT based authentication
   A user is authenticated by validating a JWT sent with the request. 

### Authorization
User authorization is based on the concepts of scopes and groups. A scope is a particular permission that is needed to access a protected resource. A group consists of set of scopes, and users are assigned to a set of groups. Therefore, when trying to access a protected resource, a user is authorized based on the scope that

## Samples

Following sample demonstrates a passthru proxy service secured with username and password
```ballerina
import ballerina/http;

http:AuthProvider basicAuthProvider = {
    scheme:"basic",
    authProvider:"config",
    issuer:"ballerina",
    keyAlias:"ballerina",
    keyPassword:"ballerina",
    keyStore:
    {
        path:"${ballerina.home}/bre/security/ballerinaKeystore.p12",
        password:"ballerina"
    }
};

endpoint http:SecureListener listener {
    port:9090,
    authProviders:[basicAuthProvider]
};

endpoint http:Client nyseEP {
    url:"http://localhost:9091",
    auth:{scheme: "jwt"}
};

@http:ServiceConfig {basePath:"/passthrough"}
service<http:Service> passthroughService bind listener {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    passthrough (endpoint caller, http:Request clientRequest) {
        var response = nyseEP -> get("/nyseStock/stocks", request = clientRequest);
        match response {
            http:Response httpResponse => {
                _ = caller -> respond(httpResponse);
            }
            http:HttpConnectorError err => {
                http:Response errorResponse = new;
                json errMsg = {"error":"error occurred while invoking the service"};
                errorResponse.setJsonPayload(errMsg);
                _ = caller -> respond(errorResponse);
            }
        }
    }
}

http:AuthProvider jwtAuthProvider = {
    scheme:"jwt",
    issuer:"ballerina",
    audience: "ballerina",
    certificateAlias: "ballerina",
    trustStore: {
        path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
        password: "ballerina"
    }
};

endpoint http:SecureListener listener1 {
    port:9092,
    authProviders:[jwtAuthProvider]
};

@http:ServiceConfig {basePath:"/nyseStock"}
service<http:Service> nyseStockQuote bind listener1 {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/stocks"
    }
    stocks (endpoint caller, http:Request clientRequest) {
        http:Response res = new;
        json payload = {"exchange":"nyse", "name":"IBM", "value":"127.50"};
        res.setJsonPayload(payload);
        _ = caller -> respond(res);
    }
}
```
## Package Contents


