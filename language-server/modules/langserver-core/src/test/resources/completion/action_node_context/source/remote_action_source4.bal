import ballerina/module1 as mod;

class TestClass {
    private mod:Client testClient;
    private boolean testField = false;

    function init(mod:Client testClient) {
        self.testClient = testClient;
    }

    public function poll(string symbolName) {
        self.testClient->p
        self.testField = true;
    }
}
