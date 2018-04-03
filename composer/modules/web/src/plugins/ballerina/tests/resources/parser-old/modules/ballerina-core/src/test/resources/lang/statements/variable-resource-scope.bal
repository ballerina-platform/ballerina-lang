import ballerina/http;

service<http> myService {
    int a = 20;

    resource myResource1(message m) {
        int b = a + 50;
        response:send(m);
    }

    resource myResource2(message m) {
        int c = b + 50;
        response:send(m);
    }

}