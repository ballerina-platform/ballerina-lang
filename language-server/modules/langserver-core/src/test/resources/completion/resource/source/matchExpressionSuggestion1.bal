import ballerina/http;

function greet () returns string|error {
    return "Hello World!";
}

service<http:Service> helloService {
    sayHello (endpoint caller, http:Request request) {
        http:Response res = new;
        string a = greet() but {
            
        };
    }
}