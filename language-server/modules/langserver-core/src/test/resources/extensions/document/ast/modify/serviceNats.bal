//import ballerina/http;
//import ballerina/nats;
//@http:ServiceConfig {
//    basePath: "/"
//}
//service hello on new http:Listener(9090) {
//    @http:ResourceConfig {
//        methods: ["GET"],
//        path: "/sayHello"
//    }
//    resource function sayHello(http:Caller caller, http:Request req) {
//        nats:Connection connection = new ();
//        nats:Producer producer = new (connection);
//        nats:Error? result = producer->publish("Foo", "Test Message");
//
//    }
//}

