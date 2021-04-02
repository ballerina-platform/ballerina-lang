import ballerina/lang.test as test;

function testFieldAccessOfNullableJSON() {
    json|error res = trap foo().name;

    error err = <error> res;
    test:assertTrue(err is runtime:JSONOperationError);
    test:assertValueEqual("{ballerina/lang.runtime}JSONOperationError", err.message());
    test:assertValueEqual("{\"message\":\"JSON value is not a mapping\"}", err.detail().toString());
}

function foo() returns (json) {
	return null;
}