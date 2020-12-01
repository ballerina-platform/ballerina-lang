import ballerina/lang.'object;

listener ABC ep = new;

int startCount = 0;
int attachCount = -2;

service on ep {

    transactional resource function get foo(string b) {
    }
}

public class ABC {

    public function 'start() returns error? {
        startCount += 1;
    }

    public function gracefulStop() returns error? {
        return ();
    }

    public function immediateStop() returns error? {
        return ();
    }

    public function attach(service object {} s, string[]|string? name = ()) returns error? {
        attachCount += 1;
    }

    public function detach(service object {} s) returns error? {
    }
}

function test () returns [int, int] {
    return [startCount, attachCount];
}
