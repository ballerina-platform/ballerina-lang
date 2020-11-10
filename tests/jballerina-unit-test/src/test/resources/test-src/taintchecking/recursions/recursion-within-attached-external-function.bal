class TestObject {
    function testFunction (string input) returns @untainted string {
        TestObject testObj = new;
        return testObj.testFunction(input);
    }
}

function secureFunction(@untainted string secureIn, string insecureIn) {

}

public function main (string... args) {
    TestObject obj = new;
    string returnValue = obj.testFunction("staticValue");
    secureFunction(returnValue, returnValue);
}
