import ballerina/module1;

public function main() returns error? {
    module1:Client cli = check new("http://localhost:8080");

    module1:Response res = cli-> get("/greet", targetType = module1:);

}
