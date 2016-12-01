package samples.passthrough;

import ballerina.lang.message;
import ballerina.net.http;

//
//Work in progress
//


@BasePath ("/account")
@Service(description = "Service which is fully comply with OpenAPI/Swagger API definitions")
service PassthroughService {

    // circuit breaker scenario

    resource passthrough (message m) {
        //...


        reply m;
    }
}