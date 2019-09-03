import ballerina/lang.'object;

listener ABC ep = new;

int startCount = 0;
int attachCount = -2;

service on ep, new PQR("pqr") {


    resource function foo(string b) {
    }

    resource function bar(string b) {
    }
}

public type ABC object {

    *'object:Listener;

    public function __start() returns error? {
        startCount += 1;
    }

    public function __gracefulStop() returns error? {
        return ();
    }

    public function __immediateStop() returns error? {
        return ();
    }

    public function __attach(service s, string? name = ()) returns error? {
        attachCount += 1;
    }

    public function __detach(service s) returns error? {
    }
};

public type PQR object {

    *'object:Listener;

    public function __init(string name){
    }

    public function __start() returns error? {
        startCount += 1;
    }

    public function __gracefulStop() returns error? {
        return ();
    }

    public function __immediateStop() returns error? {
        return ();
    }

    public function __attach(service s, string? name = ()) returns error? {
        attachCount += 1;
    }

    public function __detach(service s) returns error? {
    }
};

function test1 () returns [int, int] {
    return [startCount, attachCount];
}
