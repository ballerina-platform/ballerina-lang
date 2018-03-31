import ballerina/io;

@Description{value:"Test bal file with function in the current package"}
@Param{value:"message: Incoming message"}
function sayHello (string message){
    io:println("Hello! " + message);
}
