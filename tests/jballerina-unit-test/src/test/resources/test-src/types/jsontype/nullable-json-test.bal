function testFieldAccessOfNullableJSON() returns (json|error) {
    return foo().name;
}

function foo() returns (json) {
	return null;
}