package lang.statements.func;

import ballerina.lang.system;

public function testHelloWorldPublic() {
    testFunction();
}

function testHelloWorldPrivate() {
    testFunction();
}

function testFunction() {
	return;
}