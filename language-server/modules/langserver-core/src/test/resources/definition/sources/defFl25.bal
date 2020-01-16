public type SimpleTypeDesc int;

final [string, SimpleTypeDesc] testGlobalVar1 = ["Hello", 1];

SimpleTypeDesc testGlobalVar2 = 123;

function testFunction(int testParam) {
	[string, SimpleTypeDesc] var2 = testGlobalVar1;
	testGlobalVar2 = testParam;
}