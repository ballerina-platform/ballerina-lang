package a.b;

import ballerina.lang.system;

@Description("test method")
@Param("args: arguments")
@Return("an integer")
public function main (string[] args) (int) {
    system:println("Hello, World! "+args[0]);
}