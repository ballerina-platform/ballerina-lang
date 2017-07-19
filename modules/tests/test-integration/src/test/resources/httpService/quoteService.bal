import ballerina.lang.messages;
import ballerina.net.http;

@http:config {basePath:"/getQuote"}
service<http> quoteService {

    @http:GET{}
    @http:Path {value:"/stocks"}
    resource company (message m) {
		message response = {};
		messages:setStringPayload(response, "wso2");
		reply response;
    }

    @http:POST{}
    @http:Path {value:"/stocks"}
    resource product (message m) {
		message response = {};
		messages:setStringPayload(response, "ballerina");
		reply response;
    }

    @http:Path {value:"/stocks"}
    resource defaultStock (message m) {
		message response = {};
		messages:setHeader(response, "Method", "any");
		messages:setStringPayload(response, "default");
		reply response;
    }
}
