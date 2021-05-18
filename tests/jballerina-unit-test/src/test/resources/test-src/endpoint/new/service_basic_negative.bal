listener ABC ex = new;

service "name1" on ex {


    resource function get foo(string b) {
        self.bar(b);
    }

    remote function bar(string b) {

    }
}

string xx = "some test";

service "name1" on xx {

    resource function get foo(string b) {
    }
}

service "MyService" on ex {


    remote function foo(string b) {
    }
    remote function foo(string b, int i) {
    }
}

public class ABC {

    public function 'start() returns error?{
        return;
    }

    public function gracefulStop() returns error? {
        return ();
    }

    public function immediateStop() returns error? {
        return ();
    }

    public function attach(service object {} s, string[]|string? name = ()) returns error? {
        return ();
    }

    public function detach(service object {} s) returns error? {
    }
}

service on invalidVar {
    resource function get foo(string b) {
    }
}

service "ser2" on ex {
    private resource function get foo() {

    }

    public resource function get bar() {

    }

    public function car() {

    }

    private function dar() {

    }
}

service object { function xyz(); } def = service object {
    resource function get tuv() {
    }

    function xyz() {
    }
};

service "kgp" on ex {
    resource function get pkg() {
    }

    function gkp() {
    }
}

public function invokeServiceFunctions() {
    _ = def.tuv();
    _ = def.xyz();
    _ = kgp.pkg();
}
