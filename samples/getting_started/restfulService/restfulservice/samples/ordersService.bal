package restfulservice.samples;

import ballerina.lang.messages;
import ballerina.lang.strings;
import ballerina.net.http;

@http:BasePath{value:"/orderservice"}
service OrderMgtService {
    //Based on the HTTP method a predefined json payload is selected, it is set to the response and sent out as a HTTP response.

    @http:GET{}
    @http:POST{}
    resource orders (message m) {
        json payload = {};
        //getMethod is a function in the http package which extracts the HTTP method from a message object.
        string httpMethod = http:getMethod(m);
        if (strings:equalsIgnoreCase(httpMethod, "GET")) {
            payload = {"Order": {"ID": "111999", "Name": "ABC123","Description": "Sample order."}};

        }
        else {
            payload = {"Status":"Order is successfully added."};

        }
        message response = {};
        messages:setJsonPayload(response, payload);
        reply response;

    }

}