import ballerina/http;
import ballerina/io;

# A service representing a network-accessible API
# bound to absolute path `/hello` and port `9090`.
service /hello on new http:Listener(9090) {

    # A resource respresenting an invokable API method
    # accessible at `/hello/sayHello`.
    #
    # + caller - the client invoking this resource
    # + request - the inbound request
    resource function get sayHello(http:Caller caller, http:Request request) {

        // Send a response back to the caller.
        error? result = caller->respond("Hello Ballerina!");
        if (result is error) {
            io:println("Error in responding: ", result);
        }
    }
}
