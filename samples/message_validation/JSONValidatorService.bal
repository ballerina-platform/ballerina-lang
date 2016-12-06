package samples.passthrough;

import ballerina.lang.message;
import ballerina.net.http;

//
//Work in progress
//


@BasePath ("/passthrough")
@Service(description = "Service which is fully comply with OpenAPI/Swagger API definitions")
service PassthroughService {


    resource passthrough (message m) {
        //...
        reply m;
    }
}