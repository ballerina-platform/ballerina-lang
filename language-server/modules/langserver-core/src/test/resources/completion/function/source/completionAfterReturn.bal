import ballerina/http;

type testObject object {
    int testField = 12;
    int testPrivate = 12;
    public string testString = "hello";

    function testFunctionSignature(int param1, string param2) returns string;
};

function testReturnFunction(string param1) returns int {
    int a = 0;
    string test = "";

    return 
}
