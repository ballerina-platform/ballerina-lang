import ballerina/io;

@Description{value:"an Argument"}
@Field{value:"text: a string"}
@Field{value:"argumentId: an id"}
@Field{value:"sentiment: setiment about the argument"}
struct Argument {
    string text;
    int argumentId;
    int sentiment;
}

function main (string... args) {
    Argument arg1 = {text:"arg1", argumentId:1, sentiment:1};
    testStruct(arg1);
}

@Description{value:"Test struct data type"}
@Param{value:"argument: Incoming argument"}
function testStruct(Argument argument) {
    io:println("Hello, World!");
}
