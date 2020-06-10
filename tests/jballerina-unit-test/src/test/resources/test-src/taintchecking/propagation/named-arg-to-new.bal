import ballerina/io;

public type Client object {

    public function init(
            public string host="localhost",
            public string? user = (),
            public string? password= (),
            public string? database = (),
            public int port=3306)
        returns error? {
        bar(user);
        return ();
    }
};

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
