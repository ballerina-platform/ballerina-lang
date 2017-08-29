import ballerina.lang.messages;
import ballerina.net.http;

@http:configuration {basePath:"/getQuote"}
service<http> quoteService {

    @http:resourceConfig {
        methods:["GET"],
        path:"/stocks"
    }
    resource company (message m) {
		message response = {};
		messages:setStringPayload(response, "wso2");
		reply response;
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/stocks"
    }
    resource product (message m) {
		message response = {};
		messages:setStringPayload(response, "ballerina");
		reply response;
    }

    @http:resourceConfig {
        path:"/stocks"
    }
    resource defaultStock (message m) {
		message response = {};
		messages:setHeader(response, "Method", "any");
		messages:setStringPayload(response, "default");
		reply response;
    }
}
