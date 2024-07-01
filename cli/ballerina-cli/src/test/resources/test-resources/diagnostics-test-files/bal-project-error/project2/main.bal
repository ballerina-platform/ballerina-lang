import project2.mymath;

service / on new Listener() {

    resource function get greeting() returns string {
        return "Hello, World!";
    }

    resource function get add(int a, int b) returns int {
        return mymath:add(a, b);
    }
    
    resource function get subtract(int a, int b) returns int {
        return mymath:subtract(a, b);
    }
    
}

public class Listener {
    public isolated function 'start() returns error? {
        return;
    }
    public isolated function gracefulStop() returns error? {
        return;
    }
    public isolated function immediateStop() returns error? {
        return;
    }
    public isolated function detach(service object {} s) returns error? {
        return;
    }
    public isolated function attach(service object {} s, string[]|string? name = ()) returns error? {
        return;
    }
}
