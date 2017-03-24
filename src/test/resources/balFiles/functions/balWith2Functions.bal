import ballerina.doc;

@doc:Description{value:"test method"}
@doc:Param{value:"args: a string argument"}
@doc:Return{value:"response: an integer"}
function abc (string[] args) (int response) {
    //system:println("Hello, World! " + args[0]);
    return 5;
}

@doc:Description{value:"test method 2"}
@doc:Param{value:"args: arguments"}
@doc:Param{value:"x: integer argument"}
function xyz (string[] args, int x) {
    //system:println("Hello, World! " + args[0]);
    return;
}
