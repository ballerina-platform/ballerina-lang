package samples.passthrough;

import ballerina.lang.messages;
import ballerina.net.http;

//
//Work in progress
//


@BasePath ("/account")
@Service(description = "Service which is fully comply with OpenAPI/Swagger API definitions")
service PassthroughService {

    // circuit breaker scenario
    int a;
    resource passthrough (message m) {
        //...
    a = 10;
    reply m;
    }
}