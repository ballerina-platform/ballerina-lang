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

public class ABC {

    *'object:Listener;

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

    public function __detach(service s) returns error? {
    }
}

service on invalidVar {
    resource function foo(string b) {
    }
}

service ser2 on ex {
    private resource function foo() {

    }

    public resource function bar() {

    }

    public function car() {

    }

    private function dar() {

    }
}

service ser3 on ex {
    resource function foo() returns string? {

    }

    resource function bar() returns error {
        return error("dummy error");
    }
}

const R1 = "reason 1";
const R2 = "reason 2";

type FooErr distinct error;
type BarErr error<record { string message?; error cause?; int code; }>;

service ser4 = service {
    resource function foo() returns FooErr|BarErr {
        return FooErr(R1);
    }
};

service def = service {
    resource function tuv() {
    }

    function xyz() {
    }
};

service kgp on ex {
    resource function pkg() {
    }

    function gkp() {
    }
}

public function invokeServiceFunctions() {
    _ = def.tuv();
    _ = def.xyz();
    _ = kgp.pkg();
    _ = kgp.gkp();
}

