import ballerina/io;
import ballerina/http;

int a = 

http:Listener ep = new(9090);

public type pkg1Obj1 object {
    
};

function pkg1PrivateFunc1(int a) returns int {
    boolean s = pkg1Func1(1, 2, "hello");
    return 12;
}

public function pkg1Func1(int a, int b, string hello) returns boolean {
    io:println("pkg1Func1");

    return true;
}
