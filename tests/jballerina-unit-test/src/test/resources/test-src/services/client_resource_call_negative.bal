type CustomString string;
type CustomString2 CustomString;

string? stringNilArgument = ();
() nilArgument = ();

CustomString customString = "x";
CustomString2 customString2 = customString;

const int intConstant = 1;
var varInt = 1;
var varBoolean = true;

public function testClientResourceFunctionCallError() {
    var successClient = client object {
        resource function get path() returns int {
            return 1;
        }

        resource function post path() returns int {
            return 1;
        }

        resource function get path/[int a]() returns int {
            return 1;
        }

        resource function get path/[int a]/foo(string name) returns int {
            return 1;
        }

        resource function get path/[int a]/foo2(string name, string address) returns int {
            return 1;
        }
    };

    int result = successClient->/path/[10*11]/foo(123);
    result = successClient->/path/[1]/foo("x", "y"); // No Error occurred
    result = successClient->/path/[1]/foo("x", "y", "z");
    result = successClient->/path/[1]/foo(nilArgument);
    result = successClient->/path/[1]/foo(stringNilArgument);

    result = successClient->/path/[1]/foo(name = 23);
    result = successClient->/path/[1]/foo(name = intConstant);
    result = successClient->/path/[1]/foo(name = varInt);
    result = successClient->/path/[1]/foo(name = varBoolean);
    result = successClient->/path/[1]/foo("arg", name = 23);
    result = successClient->/path/[1]/foo(name = 23, name = 23);
    result = successClient->/path/[1]/foo();
    result = successClient->/path/[1]/foo(b = 23);
    result = successClient->/path/[1]/foo2(address = 23, "name");
    result = successClient->/path.post("a");
}

public function testResourceCallWithErrorClient() {
    var errorClient = object {
        resource function get path/[string]() returns int {
            return 1;
        }
    };
    int result = errorClient->/path/["a"];
}
