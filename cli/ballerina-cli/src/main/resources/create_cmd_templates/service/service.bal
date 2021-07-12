import ballerina/http;

# A service representing a network-accessible API
# bound to port `9090`.
service /hello on new http:Listener(9090) {

    # A resource respresenting an invokable API method
    # accessible at `/hello/sayHello`.
    # + name - the input sting name
    # + return - "Hello, " and the input string name
    resource function get sayHello(string? name=()) returns string {
        // Send a response back to the caller.

        if (name is string) {
            if !(name is "") {
                return "Hello, " + name;
            }
        }
        return "Hello, World!";
    }
}
