
public service class Service {
    remote function foo() returns int {
        return 22;
    }

    resource function get bar() returns string {
        return "bar";
    }

    resource function put bar() returns string {
        return "put-bar";
    }

    resource function get foo/bar(int i, string j) returns string {
        return i.toString() + j;
    }

    remote function getRemoteCounter(int num, decimal value, string msg = "test message") {
    }
}
