package samples.message.passthrough;

import ballerina.lang.message;
import ballerina.lang.system;
import ballerina.lang.string;
import ballerina.net.http as http;


@BasePath ("/passthrough")
service PassthroughService {

@GET
@Path ("/simple")
    resource passthrough (message m) {
            int no1;
            int no2;
            int answer;
            string payload;
            string answerStr;
            no1 = 20;
            no2 = 10;
            answer = no1 + no2;
            answerStr = string:valueOf(answer);
            payload = "Answer for 10 + 20 is " + answerStr;
            system:println("Set new string payload");
            message:setStringPayload(m, payload);
            system:println(payload);
            http:convertToResponse(m);
            reply m;
    }
}
