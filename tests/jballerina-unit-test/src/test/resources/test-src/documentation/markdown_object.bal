# Test Connector
# + url - url for endpoint
# + path - path for endpoint
class TestConnector {
    string url = "";
    string path = "";
    private string abc = ""; // Test private field

    # Test Connector action testAction
    # + return - whether successful or not
    public function testAction() returns boolean {
        boolean value = false;
        return value;
    }

    # Test Connector action testSend
    # + ep - endpoint url
    # + return - whether successful or not
    public function testSend(string ep) returns boolean {
        boolean value = false;
        return value;
    }
}

 # Test type `typeDef`
 # Test service `helloWorld`
 # Test variable `testVar`
 # Test var `testVar`
 # Test function `add`
 # Test parameter `x`
 # Test const `constant`
 # Test annotation `annot`
 # + url - url for endpoint
 # + path - path for endpoint
 class TestConnector2 {
     string url = "";
     string path = "";
     private string abc = ""; // Test private field

     # Test Connector action testAction
     # + return - whether successful or not
     public function testAction() returns boolean {
         boolean value = false;
         return value;
     }

     # Test Connector action testSend
     # + ep - endpoint url
     # + return - whether successful or not
     public function testSend(string ep) returns boolean {
         boolean value = false;
         return value;
     }
 }


