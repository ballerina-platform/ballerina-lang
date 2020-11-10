import ballerina/lang.'object;

int startCount = 0;
int attachCount = 0;

public function getStartAndAttachCount() returns string {
    return attachCount.toString() + "_" + startCount.toString();
}

public class ABC {

    *'object:Listener;

    public function init() {
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
}

public listener ABC ep = new ABC();
public listener ep1 = new ABC();
listener ep3 = new ABC();
