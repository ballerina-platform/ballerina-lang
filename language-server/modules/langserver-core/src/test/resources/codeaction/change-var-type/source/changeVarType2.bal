import ballerina/module1;

public function main() returns error? {
    module1:Client clientEP = check new("http://example.com");
    X clientResponse = clientEP->get("/");
}

type X anydata;
