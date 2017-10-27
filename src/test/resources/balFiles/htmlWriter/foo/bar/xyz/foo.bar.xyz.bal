package foo.bar.xyz;

struct Argument {
    string text;
    int argumentId;
    int sentiment;
}

@Description{value:"Remove HTTP header from the message"}
@Param{value:"m: Incoming message"}
@Param{value:"key: HTTP header key"}
function removeHeader (string m, string key) {
    println("invoked");
    Argument arg1 = {text:"arg1", argumentId:1, sentiment:1};
    testStruct(arg1);
}

@Description{value:"Test struct data type"}
@Param{value:"argument: Incoming argument"}
function testStruct(Argument argument) {
    println("Hello, World!");
}


