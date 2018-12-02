listener ABC ex = new;

service name on ex {


    resource function foo(string b) {
        self.bar(b);
    }

    resource function bar(string b) {

    }
}

string xx = "some test";

service name on xx {
    string id;

    resource function foo(string b) {
    }
}

service on ex {


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

    public function __attach(service s, map<any> annotationData) returns error? {
        return ();
    }
};

service on invalidVar {
    resource function foo(string b) {
    }
}
