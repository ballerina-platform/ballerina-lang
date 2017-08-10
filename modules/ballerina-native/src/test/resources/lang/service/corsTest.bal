import ballerina.net.http;
import ballerina.lang.messages;

@http:configuration {
    basePath:"/hello1",
    allowOrigins :["http://www.m3.com","http://hello.com"],
    allowCredentials : "true",
    allowHeaders : ["CORELATION_ID"],
    exposeHeaders : ["CORELATION_ID"],
    maxAge : "1"
}
service<http> echo1 {

    @http:resourceConfig {
         methods:["POST"],
         path : "/test1",
         allowOrigins :["http://www.wso2.com","http://facebook.com"],
         allowCredentials : "true",
         allowHeaders: ["X-Content-Type-Options","X-PINGARUNER"]
    }
    resource info1 (message m) {
        message response = {};
        json responseJson = {"echo":"resCors"};
        messages:setJsonPayload(response, responseJson);
        reply response;
    }

    @http:resourceConfig {
         methods:["GET"],
         path : "/test2"
    }
    resource info2 (message m) {
        message response = {};
        json responseJson = {"echo":"serCors"};
        messages:setJsonPayload(response, responseJson);
        reply response;
    }
}

service<http> hello2 {

    @http:resourceConfig {
         methods:["POST"],
         path : "/test1",
         allowOrigins :["http://www.chamil.com","http://facebook.com"],
         allowHeaders: ["X-Content-Type-Options","X-PINGARUNER"]
    }
    resource info1 (message m) {
        message response = {};
        json responseJson = {"echo":"cors"};
        messages:setJsonPayload(response, responseJson);
        reply response;
    }
}

@http:configuration {
    basePath:"/hello3",
    allowCredentials : "true",
    allowOrigins :["http://www.chamil.com","http://facebook.com"],
    allowHeaders: ["X-Content-Type-Options","X-PINGARUNER"],
    maxAge : "1"
}
service<http> echo3 {

    resource info1 (message m) {
        message response = {};
        json responseJson = {"echo":"cors"};
        messages:setJsonPayload(response, responseJson);
        reply response;
    }
}