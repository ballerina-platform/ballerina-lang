import ballerina/module1;

public function main() returns error? {
    module1:Client cli = check new("http://localhost:8080");

    cli-> post("/greet", "hello", targetType = module1:);

}
