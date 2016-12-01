package samples.fork-join;

import ballerina.lang.message;
import ballerina.net.http;

//
//Work in progress
//


@BasePath ("/passthrough")
@Service(description = "Airfare service")
service PassthroughService {


    resource passthrough (message m) {
        //...
        reply m;
    }
}