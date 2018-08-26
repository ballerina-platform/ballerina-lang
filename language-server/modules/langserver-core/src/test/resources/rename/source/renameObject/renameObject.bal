type TestObject object {
    function testFunction (string input) returns string;
};

function TestObject::testFunction (string input) returns string {
    return input;
}

function main (string... args) {
    TestObject obj = new;
    string returnValue = obj.testFunction("staticValue");
}
