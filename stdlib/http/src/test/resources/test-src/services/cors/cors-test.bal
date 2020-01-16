import ballerina/http;

listener http:MockListener testEP = new(9090, { host: "localhost" });

@http:ServiceConfig {
    basePath:"/hello1",
    cors:{
        allowOrigins:["http://www.m3.com", "http://www.hello.com"],
        allowCredentials:true,
        allowHeaders:["CORELATION_ID"],
        exposeHeaders:["CORELATION_ID"],
        maxAge:1
    }
}
service echo1 on testEP {

    @http:ResourceConfig {
        methods:["POST"],
        path : "/test1",
        cors: {
            allowOrigins :["http://www.wso2.com", "http://www.facebook.com"],
            allowCredentials : true,
            allowHeaders:["X-Content-Type-Options", "X-PINGOTHER"]
        }
    }
    resource function info1 (http:Caller caller, http:Request req) {
        http:Response res = new;
        json responseJson = {"echo":"resCors"};
        res.setJsonPayload(responseJson);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
         methods:["GET"],
         path : "/test2"
    }
    resource function info2 (http:Caller caller, http:Request req) {
        http:Response res = new;
        json responseJson = {"echo":"serCors"};
        res.setJsonPayload(responseJson);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path : "/test3",
        cors:{
            allowOrigins :["http://www.wso2.com", "http://facebook.com", "http://www.amazon.com"],
            allowCredentials:true
        }
    }
    resource function info3 (http:Caller caller, http:Request req) {
        http:Response res = new;
        json responseJson = {"echo":"moreOrigins"};
        res.setJsonPayload(responseJson);
        checkpanic caller->respond(res);
    }
}

service hello2 on testEP {

    @http:ResourceConfig {
         methods:["POST"],
         path : "/test1",
         cors: {
            allowOrigins :["http://www.hello.com", " http://www.facebook.com  "],
            exposeHeaders:["X-Content-Type-Options", "X-PINGOTHER"]
        }
    }
    resource function info1 (http:Caller caller, http:Request req) {
        http:Response res = new;
        json responseJson = {"echo":"resOnlyCors"};
        res.setJsonPayload(responseJson);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["PUT"],
        path : "/test2",
        cors:{
            allowMethods :["HEAD", "PUT"],
            allowOrigins:["http://www.bbc.com", " http://www.amazon.com  "],
            exposeHeaders:["X-Content-Type-Options", "X-PINGOTHER"]
        }
    }
    resource function info2 (http:Caller caller, http:Request req) {
        http:Response res = new;
        json responseJson = {"echo":"optionsOnly"};
        res.setJsonPayload(responseJson);
        checkpanic caller->respond(res);
    }
}

@http:ServiceConfig {
    basePath:"/hello3",
    cors:{
        allowCredentials : true,
        allowMethods:["GET", "PUT"],
        allowOrigins:["http://www.m3.com", "http://www.facebook.com"],
        allowHeaders:["X-Content-Type-Options", "X-PINGOTHER"],
        maxAge:1
    }
}
service echo3 on testEP {

    @http:ResourceConfig {
        methods:["POST", "PUT"]
    }
    resource function info1 (http:Caller caller, http:Request req) {
        http:Response res = new;
        json responseJson = {"echo":"cors"};
        res.setJsonPayload(responseJson);
        checkpanic caller->respond(res);
    }
}

service echo4 on testEP {
    @http:ResourceConfig {
        methods:["POST"]
    }
    resource function info1 (http:Caller caller, http:Request req) {
        http:Response res = new;
        json responseJson = {"echo":"noCors"};
        res.setJsonPayload(responseJson);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["OPTIONS"]
    }
    resource function info2 (http:Caller caller, http:Request req) {
        http:Response res = new;
        json responseJson = {"echo":"noCorsOPTIONS"};
        res.setJsonPayload(responseJson);
        checkpanic caller->respond(res);
    }
}
