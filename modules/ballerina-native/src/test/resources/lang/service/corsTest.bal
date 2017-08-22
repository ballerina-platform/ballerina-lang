import ballerina.net.http;
import ballerina.lang.messages;

@http:configuration {
    basePath:"/hello1",
    allowOrigins :["http://www.m3.com", "http://www.hello.com"],
    allowCredentials : true,
    allowHeaders : ["CORELATION_ID"],
    exposeHeaders : ["CORELATION_ID"],
    maxAge : 1
}
service<http> echo1 {

    @http:resourceConfig {
         methods:["POST"],
         path : "/test1",
         allowOrigins :["http://www.wso2.com", "http://www.facebook.com"],
         allowCredentials : true,
         allowHeaders: ["X-Content-Type-Options", "X-PINGOTHER"]
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

    @http:resourceConfig {
        methods:["POST"],
        path : "/test3",
        allowOrigins :["http://www.wso2.com", "http://facebook.com", "http://www.amazon.com"],
        allowCredentials : true
    }
    resource info3 (message m) {
        message response = {};
        json responseJson = {"echo":"moreOrigins"};
        messages:setJsonPayload(response, responseJson);
        reply response;
    }
}

service<http> hello2 {

    @http:resourceConfig {
         methods:["POST"],
         path : "/test1",
         allowOrigins :["http://www.hello.com"," http://www.facebook.com  "],
         exposeHeaders: ["X-Content-Type-Options","X-PINGOTHER"]
    }
    resource info1 (message m) {
        message response = {};
        json responseJson = {"echo":"resOnlyCors"};
        messages:setJsonPayload(response, responseJson);
        reply response;
    }

    @http:resourceConfig {
        methods:["PUT"],
        path : "/test2",
        allowMethods :["HEAD", "PUT"],
        allowOrigins :["http://www.bbc.com"," http://www.amazon.com  "],
        exposeHeaders: ["X-Content-Type-Options","X-PINGOTHER"]
    }
    resource info2 (message m) {
        message response = {};
        json responseJson = {"echo":"optionsOnly"};
        messages:setJsonPayload(response, responseJson);
        reply response;
    }
}

@http:configuration {
    basePath:"/hello3",
    allowCredentials : true,
    allowMethods :["GET", "PUT"],
    allowOrigins :["http://www.m3.com","http://www.facebook.com"],
    allowHeaders: ["X-Content-Type-Options","X-PINGOTHER"],
    maxAge : 1
}
service<http> echo3 {

    @http:resourceConfig {
        methods:["POST", "PUT"]
    }
    resource info1 (message m) {
        message response = {};
        json responseJson = {"echo":"cors"};
        messages:setJsonPayload(response, responseJson);
        reply response;
    }

    @http:resourceConfig {
        methods:["GET"]
    }
    resource info2 (message m) {
        message result = {};
        m -> sampleWorker;
        result <- sampleWorker;
        reply result;
        worker sampleWorker {
            m <- default;
            message msg = {};
            json responseJson = {"echo":"worker"};
            messages:setJsonPayload( msg, responseJson);
            msg -> default;
        }
    }
}

service<http> echo4 {
    @http:resourceConfig {
        methods:["POST"]
    }
    resource info1 (message m) {
        message response = {};
        json responseJson = {"echo":"noCors"};
        messages:setJsonPayload(response, responseJson);
        reply response;
    }

    @http:resourceConfig {
        methods:["OPTIONS"]
    }
    resource info2 (message m) {
        message response = {};
        json responseJson = {"echo":"noCorsOPTIONS"};
        messages:setJsonPayload(response, responseJson);
        reply response;
    }
}