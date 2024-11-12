client class Client {
    # Test Description.
    #
    # + param1 - parameter description
    # + param2 - parameter description
    # + param3 - parameter description
    # + return - return value description
    resource function post path/[string param1]/path2/[string ...param2](string param3, int ...param4) returns string {
        return "post";
    }

    # Description.
    #
    # + ids - parameter description
    # + return - return value description
    resource function get [int... ids]() returns string {
        return "get";
    }

    # Description.
    # + return - return value description
    resource function get path2/[int... ]() returns string {
        return "get";
    }
}

public function test() {
    Client cli = new Client();
    string stringResult = cli->/path/[""]/path2/[""].post("", 1);
    string stringResult2 = cli->/[0];
    string stringResult3 = cli->/[0];
}
