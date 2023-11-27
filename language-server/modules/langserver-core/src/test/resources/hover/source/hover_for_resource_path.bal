client class Client {
    # Test Description.
    #
    # + param1 - parameter description
    # + param2 - parameter description
    # + param3 - parameter description
    # + param4 - parameter description
    # + return - return value description
    resource function post path/[string param1]/path2/[string ...param2](string param3, int ...param4) returns string {
        return "post";
    }
}

public function test() {
    Client cli = new Client();
    string stringResult = cli->/path/[""]/path2/[""].post("", 1);
}
