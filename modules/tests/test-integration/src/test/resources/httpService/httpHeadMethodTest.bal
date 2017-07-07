import ballerina.net.http;
import ballerina.lang.messages;

@http:config {basePath:"/headQuote"}
service<http> headQuoteService {

    @http:HEAD{}
    resource stocks (message m) {   
	message response = {};
	messages:setStringPayload(response, "wso2");
	reply response;
    }
}
