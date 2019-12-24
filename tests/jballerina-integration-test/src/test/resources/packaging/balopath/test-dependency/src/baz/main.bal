import ballerina/io;
import wso2test/foo;

# Prints `Hello World`.

public const string bazStr1 = "this is a baz string";

public function main() {
    io:println("Hello World!");
}

public function bazFn() returns string {
    string testStr1 = foo:fooStr1;
    string testStr2 = foo:fooFn();
    string msg = "invoked bazFn";
    io:println(msg);
    return msg;
}
