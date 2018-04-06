type TestObject object {
    function testFunction (string input) returns string;
};

function TestObject::testFunction (string input) returns string {
    return input;
}

function secureFunction(@sensitive string secureIn, string insecureIn) {

}

public function main (string[] args) {
    TestObject obj = new;
    string returnValue = obj.testFunction(args[0]);
    secureFunction(returnValue, returnValue);
}
