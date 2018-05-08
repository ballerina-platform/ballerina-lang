
import ballerina/lang.messages;
import ballerina/lang.strings;
import ballerina/http;

@http:configuration {basePath:"/orderservice"}
service<http> OrderMgtService {

    @http:resourceConfig {
        methods:["GET", "POST"]
    }
    resource orders (message m) {
        json payload = {};
        string httpMethod = http:getMethod(m);
        if (strings:equalsIgnoreCase(httpMethod, "GET")) {
            payload = {"Order": {"ID": "111999", "Name": "ABC123","Description": "Sample order."}};

        }
        else {
            payload = {"Status":"Order is successfully added."};

        }
        message response = {};
        messages:setJsonPayload(response, payload);
        response:send(response);

    }

}