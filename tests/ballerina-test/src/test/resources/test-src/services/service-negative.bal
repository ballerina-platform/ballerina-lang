import ballerina.io;
import ballerina.net.http;
service<http> FooService {

    resource test1 (string dummyParam) {
        io:println("test1-before");
        break;
        io:println("test1-after");
    }

    resource test2 (string dummyParam) {
        io:println("test2-before");
        next;
        io:println("test2-after");
    }

    resource test3 (string dummyParam) {
        io:println("test3-before");
        abort;
        io:println("test3-after");
    }

    resource test4 (string dummyParam) {
        io:println("test4-before");
        return;
        io:println("test4-after");
    }

    resource test5 (string dummyParam) {
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
