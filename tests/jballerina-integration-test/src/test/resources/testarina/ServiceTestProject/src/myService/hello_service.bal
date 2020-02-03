// A system module containing protocol access constructs
// Module objects referenced with 'http:' in code
import ballerina/http;
import ballerina/io;

# A service is a network-accessible API
# Advertised on '/hello', port comes from listener endpoint
service hello on new http:Listener(9393) {

    # A resource is an invokable API method
    # Accessible at '/hello/sayHello
    # 'caller' is the client invoking this resource 

    # + caller - Server Connector
    # + request - Request
    resource function sayHello(http:Caller caller, http:Request request) {
        // Send a response back to caller
        // -> indicates a synchronous network-bound call
        error? result = caller->respond("Hello Test!");
        if (result is error) {
            io:println("Error in responding", result);
        }
    }
}
