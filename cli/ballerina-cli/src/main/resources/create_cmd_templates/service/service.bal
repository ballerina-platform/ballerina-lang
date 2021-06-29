import ballerina/http;

# A service representing a network-accessible API
# bound to port `9090`.
service /hello on new http:Listener(9090) {

    # + name - the input sting name
    # + return - string name with hello message or error
    resource function get sayHello(string name) returns string|error {
        // Send a response back to the caller.
        if !(name is "") {
            return "Hello, " + name;
        }
        return error("name should not be empty!");
    }
}
