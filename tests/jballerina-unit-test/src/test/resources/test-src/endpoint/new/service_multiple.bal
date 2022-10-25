listener ABC ep = new;

int startCount = 0;
int attachCount = -2;

service on ep, new PQR("pqr") {


    resource function get foo(string b = "") {
    }

    resource function get bar(string b = "") {
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

public class PQR {

    public function init(string name){
    }

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

function test1 () returns [int, int] {
    return [startCount, attachCount];
}
