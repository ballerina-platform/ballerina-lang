class TestObject {

    public string testString;

    public function init(string testString) {
        self.testString = testString;
    }

    function testFunction(string input) returns string {
        return input + self.testString;
    }

    function testFunctionWrapped(string input) returns string {
        return self.testFunction(input);
    }

    function testFunctionIsNotInvolvedInReturnValue(string input) returns string {
        _ = self.testFunction(input);
        return input;
    }
}

function secureFunction(@untainted string secureIn, string insecureIn) {

}

public function main (string... args) {
    TestObject obj = new(args[0]);
    string returnValue = obj.testFunction("staticValue");
    secureFunction(returnValue, returnValue);
    secureFunction(obj.testFunctionWrapped("staticValue"), "JustText");
    secureFunction(obj.testFunctionIsNotInvolvedInReturnValue("staticValue"), "JustText");
}
