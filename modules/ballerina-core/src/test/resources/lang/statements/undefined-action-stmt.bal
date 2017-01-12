import ballerina.net.http;

public function testUndefinedFunction() {
    http:HTTPConnector localhostEP = new http:HTTPConnector("http://localhost:9090");
    http:HTTPConnector.foo(localhostEP);
}

