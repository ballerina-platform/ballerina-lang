import ballerina/io;
import ballerina/net.http;
import ballerina/net.http.mock;

endpoint mock:NonListeningServiceEndpoint echoEP {
    port:9090
};

service<http:Service> FooService {

    test1 (string dummyParam) {
        io:println("test1-before");
        break;
        io:println("test1-after");
    }

    test2 (string dummyParam) {
        io:println("test2-before");
        next;
        io:println("test2-after");
    }

    test3 (string dummyParam) {
        io:println("test3-before");
        abort;
        io:println("test3-after");
    }

    test4 (string dummyParam) {
        io:println("test4-before");
        return;
        io:println("test4-after");
    }

    test5 (string dummyParam) {
        worker w1 {
            var a = "a";
            a -> w2;
        }
        worker w2 {
            var b = "b";
            b -> w1;
        }
    }
}
