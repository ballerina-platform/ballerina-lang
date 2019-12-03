import ballerinax/java;

handle h1 = java:fromString("John");
handle h2 = java:fromString("Doe");

public function mockedNativeFuncWithOptionalParams(int a, float b, handle c = h1, int d = 5, handle e = h2)
                                                    returns [int, float, string, int, string] = @java:Method {
    name: "mockedNativeFuncWithOptionalParams",
    class: "org.ballerinalang.test.functions.FunctionSignatureTest"
} external;

function testOptionalArgsInNativeFunc() returns [int, float, string, int, string] {
    return mockedNativeFuncWithOptionalParams(78, 89.0);
}
