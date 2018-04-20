
public native function mockedNativeFuncWithOptionalParams(int a, float b, string c = "John", int d = 5, string e = "Doe", int... z) returns (int, float, string, int, string, int[]);

function testOptionalArgsInNativeFunc() returns (int, float, string, int, string, int[]) {
	return mockedNativeFuncWithOptionalParams(78, 89, 4,5,6);
}
