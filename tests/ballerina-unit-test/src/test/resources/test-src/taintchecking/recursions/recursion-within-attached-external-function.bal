type TestObject object {
    function testFunction (string input) returns string;
};

function TestObject::testFunction (string input) returns @untainted string {
    TestObject testObj = new;
    return testObj.testFunction(input);
}

function secureFunction(@sensitive string secureIn, string insecureIn) {

}

function main (string... args) {
    TestObject obj = new;
    string returnValue = obj.testFunction("staticValue");
    secureFunction(returnValue, returnValue);
}
