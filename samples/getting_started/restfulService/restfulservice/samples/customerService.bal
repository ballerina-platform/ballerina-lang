package restfulservice.samples;

import ballerina.lang.messages;
import ballerina.lang.strings;
import ballerina.net.http;

@http:BasePath{value:"/customerservice"}
service CustomerMgtService {
    //Based on the HTTP method a predefined json payload is selected, it is set to response object and sent out as a HTTP response.
    @http:GET{}
    @http:POST{}
    resource customers (message m) {
        json payload = {};
        //getMethod is a function in the http package which extracts the HTTP method from a message object.
        string httpMethod = http:getMethod(m);
        if (strings:equalsIgnoreCase(httpMethod, "GET")) {
            payload = {"Customer": {"ID": "987654", "Name": "ABC PQR","Description": "Sample Customer."}};

        }
        else {
            payload = {"Status":"Customer is successfully added."};

        }
        message response = {};
        messages:setJsonPayload(response, payload);
        reply response;

    }

}