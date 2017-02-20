package restfulservice.samples;
import ballerina.lang.messages;
import ballerina.lang.strings;
import ballerina.net.http;
@http:BasePath("/orderservice")
service OrderMgtService {

    @http:GET
    @http:POST
    resource orders (message m) {
        json payload = {};
        string httpMethod = http:getMethod(m);
        if (strings:equalsIgnoreCase(httpMethod, "GET")) {
            payload = `{"Order": {"ID": "111999", "Name": "ABC123","Description": "Sample order."}}`;

        }
        else {
            payload = `{"Status":"Order is successfully added."}`;

        }
        message response = {};
        messages:setJsonPayload(response, payload);
        reply response;

    }

}