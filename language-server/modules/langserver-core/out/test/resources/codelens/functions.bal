import ballerina/http;
import ballerina/task;
import ballerina/io;
import ballerina/websub;

http:Client client2 = new("https://postman-echo.com/get?foo1=bar1&foo2=bar2");

public type ObjectName object {

};

public type RecordName record {

};

public function returnInt(int n1, int n2, int n3) returns int {
    return 1;
}

public function returnTuple(int n1, int n2, int n3) returns (int, float) {
    io:println("");
    return (300, 4.0);
}

public function returnUnion(int n1, int n2, int n3) returns (int|string) {
    return 5000;
}

# Description
#
# + n1 - n1 Parameter Description
# + n2 - n2 Parameter Description
# + n3 - n3 Parameter Description
# + return - Return Value Description
public function returnMap(int n1, int n2, int n3) returns map<string> {
    map<string> m = { key: "n1" };
    return m;
}

public function complexInput(task:Timer timer) returns error? {
    return ();
}

function complexReturnType(string url) returns http:Client {
    http:Client myclient = new("https://postman-echo.com/get?foo1=bar1&foo2=bar2");
    return myclient;
}
