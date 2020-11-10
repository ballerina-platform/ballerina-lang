class TestObject {
    string 'field = "";
    function testFunction (string input) returns string {
        return input;
    }

    function setField(string newVal) {
        self.'field = newVal;
    }
}

function secureFunction(@untainted string secureIn, string insecureIn) {

}


@tainted TestObject G1 = new();

public function main (string... args) {
    G1.setField(args[0]);

    TestObject obj = new;
    string returnValue = obj.testFunction("staticValue");
    secureFunction(returnValue, returnValue);
}
