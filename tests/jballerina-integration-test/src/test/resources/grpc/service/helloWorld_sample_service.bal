import ballerina/grpc;

listener grpc:Listener ep = new (9000);

service helloWorld on ep {

    resource function hello(grpc:Caller caller, HelloRequest value) {
        // Implementation goes here.

        // You should return a HelloResponse
    }
    resource function bye(grpc:Caller caller, ByeRequest value) {
        // Implementation goes here.

        // You should return a ByeResponse
    }
}

