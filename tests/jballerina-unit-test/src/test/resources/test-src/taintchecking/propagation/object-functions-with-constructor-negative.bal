type TestObject object {

    public string testString;

    public function __init (string testString) {
        self.testString = testString;
    }

    function testFunction (string input) returns string {
        return input;
    }
};

function secureFunction(@untainted string secureIn, string insecureIn) {

}

public function main (string... args) {
    TestObject obj = new (args[0]);
    string returnValue = obj.testFunction("staticValue");
    secureFunction(returnValue, returnValue);
}
