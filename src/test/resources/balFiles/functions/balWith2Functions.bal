package a.b;

import ballerina.lang.system;

@Description("test method")
@Param("args: arguments")
@Return("an integer")
public function main (string[] args) (int) {
    system:println("Hello, World! "+args[0]);
    return 5;
}

@Description("test method 2")
@Param("args: arguments")
@Param("x: integer argument")
public function main (string[] args, int x) {
    system:println("Hello, World! "+args[0]);
}