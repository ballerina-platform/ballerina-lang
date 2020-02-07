import ballerina/lang.'object;

int startCount = 0;
int attachCount = 0;

public function getStartAndAttachCount() returns string {
    return attachCount.toString() + "_" + startCount.toString();
}

public type ABC object {

    *'object:Listener;

    public function __init() {
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

public listener ABC ep = new ABC();
