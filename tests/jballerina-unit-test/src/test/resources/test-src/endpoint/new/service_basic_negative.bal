import ballerina/lang.'object;

listener ABC ex = new;

service name1 on ex {


    resource function foo(string b) {
        self.bar(b);
    }

    resource function bar(string b) {

    }
}

string xx = "some test";

service name1 on xx {

    resource function foo(string b) {
    }
}

service MyService on ex {


    resource function foo(string b) {
    }
    function foo(string b) {
    }
}

public type ABC object {

    *'object:AbstractListener;

    public function __start() returns error?{
        return;
    }

    public function __gracefulStop() returns error? {
        return ();
    }

    public function __immediateStop() returns error? {
        return ();
    }

    public function __attach(service s, string? name = ()) returns error? {
        return ();
    }
};

service on invalidVar {
    resource function foo(string b) {
    }
}

service ser2 on ex {
    private resource function foo() {

    }

    public resource function bar() {

    }
}
