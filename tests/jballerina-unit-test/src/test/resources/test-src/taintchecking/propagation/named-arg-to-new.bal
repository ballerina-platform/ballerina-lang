import ballerina/io;

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
    io:println("Completed");
}

function callClient(string username) {
     Client|error p1 = new(user=username);
     io:println("Call Completed");
}

function bar(@untainted string? arg) {

}
