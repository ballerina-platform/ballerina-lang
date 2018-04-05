type TestObject object {
    function testFunction (string input) returns string {
        return input;
    }
};

function secureFunction(@sensitive string secureIn, string insecureIn) {

}

public function main (string[] args) {
    TestObject obj = new;
    string returnValue = obj.testFunction("staticValue");
    secureFunction(returnValue, returnValue);
}
