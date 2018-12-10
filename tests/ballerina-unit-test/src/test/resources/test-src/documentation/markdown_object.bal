# Test Connector
# + url - url for endpoint
# + path - path for endpoint
type TestConnector object {
    string url = "";
    string path = "";
    private string abc = ""; // Test private field

    # Test Connector action testAction
    # + return - whether successful or not
    public function testAction() returns boolean;

    # Test Connector action testSend
    # + ep - endpoint url
    # + return - whether successful or not
    public function testSend(string ep) returns boolean;
};

# overriden description
# + return - description
function TestConnector.testAction() returns boolean {
    boolean value = false;
    return value;
}

# overriden description
# + ep - endpoint
# + return - description
function TestConnector.testSend(string ep) returns boolean {
    boolean value = false;
    return value;
}

