import ballerina.lang.system;
import ballerina.doc;

@doc:Description{value:"an Argument"}
@doc:Field{value:"text: a string"}
@doc:Field{value:"argumentId: an id"}
@doc:Field{value:"sentiment: setiment about the argument"}
struct Argument {
    string text;
    int argumentId;
    int sentiment;
}

function main (string[] args) {
    Argument arg1 = {text:"arg1", argumentId:1, sentiment:1};
    testStruct(arg1);
}

@doc:Description{value:"Test struct data type"}
@doc:Param{value:"argument: Incoming argument"}
function testStruct(Argument argument) {
    system:println("Hello, World!");
}