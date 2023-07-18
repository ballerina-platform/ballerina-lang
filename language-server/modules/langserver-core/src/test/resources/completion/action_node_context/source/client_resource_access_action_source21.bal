import ballerina/module1;

public function main() {
    module1:Client clientEndpoint = new("http://localhost:9090");
    
    clientEndpoint -> /
}
