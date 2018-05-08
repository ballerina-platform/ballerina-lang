
import ballerina/lang.messages;
import ballerina/lang.strings;
import ballerina/http;

@http:configuration{basePath:"/customerservice"}
service<http> CustomerMgtService {

    @http:resourceConfig {
        methods:["GET", "POST"]
    }
    resource customers (message m) {
        json payload = {};
        string httpMethod = http:getMethod(m);
        if (strings:equalsIgnoreCase(httpMethod, "GET")) {
            payload = {"Customer": {"ID": "987654", "Name": "ABC PQR","Description": "Sample Customer."}};

        }
        else {
            payload = {"Status":"Customer is successfully added."};

        }
        message response = {};
        messages:setJsonPayload(response, payload);
        response:send(response);

    }

}