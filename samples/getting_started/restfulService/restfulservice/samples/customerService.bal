package restfulservice.samples;
import ballerina.lang.messages;
import ballerina.lang.strings;
import ballerina.net.http;
@http:BasePath("/customerservice")
service CustomerMgtService {

    @http:GET
    @http:POST
    resource customers (message m) {
        json payload = {};
        string httpMethod = http:getMethod(m);
        if (strings:equalsIgnoreCase(httpMethod, "GET")) {
            payload = `{"Customer": {"ID": "987654", "Name": "ABC PQR","Description": "Sample Customer."}}`;

        }
        else {
            payload = `{"Status":"Customer is successfully added."}`;

        }
        message response = {};
        messages:setJsonPayload(response, payload);
        reply response;

    }

}