package samples.passthrough;

import ballerina.lang.message;
import ballerina.net.http;

//
//Work in progress
//


@BasePath ("/passthrough")
@Service(description = "Airfare service")
service ATMLocatorService {


    resource passthrough (message m) {
        //...
        reply m;
    }
}