function testFieldAccessOfNullableJSON() returns (json) {
    json j1 = foo().name;
    return j1;
}

function foo() returns (json) {
	return null;
}