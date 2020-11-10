class TestObject {
    public string testString;

    public function init (string testString) {
        self.testString = testString;
    }

    function testFunction (string input) returns string {
        return input;
    }
}

function secureFunction(@untainted string secureIn, string insecureIn) {

}

public function main (string... args) {
    TestObject obj = new ("staticValue");
    string returnValue = obj.testFunction("staticValue");
    secureFunction(returnValue, returnValue);
}
