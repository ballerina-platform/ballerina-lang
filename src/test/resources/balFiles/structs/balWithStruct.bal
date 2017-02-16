import ballerina.lang.system;

struct Argument {
    string text;
    int argumentId;
    int sentiment;
}

function main (string[] args) {
    Argument arg1 = {text:"arg1", argumentId:1, sentiment:1};
    testStruct(arg1);
}

@Description("Test struct data type")
@Param("argument: Incoming argument")
function testStruct(Argument argument) {
    system:println("Hello, World!");
}