
type E1 error;

public isolated client class GreeterClient {

    isolated remote function sayHello(HelloRequest req) returns HelloReply|E1 {
        return {
            message: "Hello"
        };
    }
}

public type HelloReply record {|
    string message = "";
|};

type HelloRequest record {
    string name;
};

GreeterClient ep = new();
public function main() {
    ep->sayHello({name: "Ballerina"});
}