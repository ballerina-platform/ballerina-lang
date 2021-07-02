
# Given the last used ID, this will return the next ID to be used
#
# + lastId - Last used ID
# + return - Next ID to be used
public function generateId(int lastId) returns int {
    return lastId + 1;
}

# Description
service helloService on new MockListener(8080) {

    # Description
    #
    # + caller - Parameter Description  
    # + request - Parameter Description
    @ResourceConfig {
        path: "/sayHello"
    }
    resource function sayHello(MockCaller caller, MockRequest request) {
        //io:println("Hello World!!");
    }
}
