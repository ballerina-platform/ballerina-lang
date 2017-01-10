package lang.statements.func;

import ballerina.lang.system;

public function testHelloWorldPublic() {
    system:println("Hello world. I am a public function.");
}

function testHelloWorldPrivate() {
    system:println("Hello world. I am a private function.");
}
