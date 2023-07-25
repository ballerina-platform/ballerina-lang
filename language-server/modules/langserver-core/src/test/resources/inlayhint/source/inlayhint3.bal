public client class Client {
    resource function get hello(string hello, string... name) returns string {
        return "Hello" + name;
    }
}

public function test() {
    Client cl = new();
    string result  = cl -> /hello("Hello", "Ballerina");
}
