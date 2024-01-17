import ballerina/module1;

public function main() returns error? {
    module1:Client clientEP = check new("http://example.com");
    x clientResponse = clientEP->get("/");
}
