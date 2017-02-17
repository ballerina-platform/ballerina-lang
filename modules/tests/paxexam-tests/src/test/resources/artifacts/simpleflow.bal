package samples.message.passthrough;

import ballerina.lang.messages;
import ballerina.lang.system;
import ballerina.lang.strings;
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
            answerStr = strings:valueOf(answer);
            payload = "Answer for 10 + 20 is " + answerStr;
            system:println("Set new string payload");
            messages:setStringPayload(m, payload);
            system:println(payload);
            http:convertToResponse(m);
            reply m;
    }
}
