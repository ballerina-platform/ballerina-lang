import wso2test/foo;

# Prints `Hello World`.

public const string bazStr1 = "this is a baz string";

public function main() {
    string s = "Hello World!";
}

public function bazFn() returns string {
    string testStr1 = foo:fooStr1;
    string testStr2 = foo:fooFn();
    return "invoked bazFn";
}
