import ballerina/io;
import ballerina/http;

listener http:MockListener echoEP = new(9090);

service FooService {

    test1 (string dummyParam) {
        io:println("test1-before");
        break;
        io:println("test1-after");
    }

    test2 (string dummyParam) {
        io:println("test2-before");
        continue;
        io:println("test2-after");
    }

    test3 (string dummyParam) {
        io:println("test3-before");
        abort;
        io:println("test3-after");
    }

    test4 (string dummyParam) {
        io:println("test4-before");
        done;
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
