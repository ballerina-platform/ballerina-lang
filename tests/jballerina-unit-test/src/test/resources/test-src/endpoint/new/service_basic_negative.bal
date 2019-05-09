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
    string id;

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

    *AbstractListener;

    public function __start() returns error?{
        return;
    }

    public function __stop() returns error? {
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
