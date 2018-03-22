@Description{value:"test method"}
@Param{value:"args: a string argument"}
@Return{value:"response: an integer"}
function abc (string[] args) returns (int) {
    //system:println("Hello, World! " + args[0]);
    return 5;
}

@Description{value:"test method 2"}
@Param{value:"args: arguments"}
@Param{value:"x: integer argument"}
function xyz (string[] args, int x) {
    //system:println("Hello, World! " + args[0]);
    return;
}
