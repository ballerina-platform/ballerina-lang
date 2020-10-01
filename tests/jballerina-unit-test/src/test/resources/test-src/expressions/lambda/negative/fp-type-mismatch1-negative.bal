function test1() returns (boolean) {
     function (string, int) returns (boolean) foo = test2;
     return foo("this test fails at line", 2);
}

function test2(string a, float b) returns (boolean){
    return false;
}

type FunctionType function (Context, anydata) returns json|error; // Unknown type `Context` in param
type FunctionEntry [FunctionType, typedesc<anydata>];
map<FunctionEntry> functions = { };
