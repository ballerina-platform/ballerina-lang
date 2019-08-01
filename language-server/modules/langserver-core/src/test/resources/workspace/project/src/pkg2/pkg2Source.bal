// A system package containing protocol access constructs
// Package objects referenced with 'http:' in code
import ballerina/http;

listener http:Listener listenerEp = new(9090); 

# A service is a network-accessible API
# Advertised on '/hello', port comes from listener endpoint
service testDocService on listenerEp {

    # A resource is an invokable API method
    # Accessible at '/hello/sayHello
    # 'caller' is the client invoking this resource
    #
    # + caller - Server Connector
    # + request - Request
    resource function sayHello(http:Caller caller, http:Request request) {

        // Create object to carry data back to caller
        http:Response response = new;

        // Objects and structs can have function calls
        response.setTextPayload("Hello Ballerina!\n");

        // Send a response back to caller
        // Errors are ignored with '_'
        // -> indicates a synchronous network-bound call
        checkpanic caller->respond(response);
    }
}
