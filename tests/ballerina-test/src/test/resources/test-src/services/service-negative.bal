import ballerina.net.http;

service<http> FooService {

    resource test1 (string dummyParam) {
        println("test1-before");
        break;
        println("test1-after");
    }

    resource test2 (string dummyParam) {
        println("test2-before");
        next;
        println("test2-after");
    }

    resource test3 (string dummyParam) {
        println("test3-before");
        abort;
        println("test3-after");
    }

    resource test4 (string dummyParam) {
        println("test4-before");
        return;
        println("test4-after");
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
