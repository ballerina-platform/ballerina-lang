import ballerina/jballerina.java;

public class Client {

    public function init(
            string host="localhost",
            string? user = (),
            string? password= (),
            string? database = (),
            int port=3306)
        returns error? {
        bar(user);
        return ();
    }
}

public function main(string... argv) {
    callClient(argv[0]);
    println("Completed");
}

function callClient(string username) {
     Client|error p1 = new(user=username);
     println("Call Completed");
}

function bar(@untainted string? arg) {

}

public function println(any|error... values) = @java:Method {
    'class: "org.ballerinalang.test.utils.interop.Utils"
} external;
