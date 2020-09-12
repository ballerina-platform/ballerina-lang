import ballerina/java;

public function mockedNativeFuncWithOptionalParams(int a, float b, string c = "John", int d = 5, string e = "Doe")
                                                    returns [int, float, string, int, string] = @java:Method {
    name: "mockedNativeFuncWithOptionalParams",
    'class: "org.ballerinalang.test.functions.FunctionSignatureTest"
} external;

function testOptionalArgsInNativeFunc() returns [int, float, string, int, string] {
    return mockedNativeFuncWithOptionalParams(78, 89.0);
}
