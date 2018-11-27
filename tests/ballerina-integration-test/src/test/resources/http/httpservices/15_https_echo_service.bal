import ballerina/http;

listener http:Listener echoDummyEP = new(9109);

listener http:Listener echoHttpEP = new(9110);

http:ServiceEndpointConfiguration echoEP2Config = {
    secureSocket: {
        keyStore: {
            path:"${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password:"ballerina"
        }
    }
};

listener http:Listener echoEP2 = new(9111, config = echoEP2Config);

@http:ServiceConfig {
    basePath:"/echo"
}

service echo2 on echoEP2 {
    @http:ResourceConfig {
        methods:["POST"],
        path:"/"
    }
    resource function echo2(http:Caller caller, http:Request req) {
        http:Response res = new;
        res.setTextPayload("hello world");
        _ = caller->respond(res);
    }
}

@http:ServiceConfig  {
    basePath:"/echoOne"
}
service echoOne1 on echoEP2 {
    @http:ResourceConfig {
        methods:["POST"],
        path:"/abc"
    }
    resource function echoAbc(http:Caller caller, http:Request req) {
        http:Response res = new;
        res.setTextPayload("hello world");
        _ = caller->respond(res);
    }
}

@http:ServiceConfig {
    basePath:"/echoDummy"
}
service echoDummy1 on echoDummyEP {

    @http:ResourceConfig {
        methods:["POST"],
        path:"/"
    }
    resource function echoDummy1(http:Caller caller, http:Request req) {
        http:Response res = new;
        res.setTextPayload("hello world");
        _ = caller->respond(res);
    }
}
