listener ABC ep = new;

service on ep {


    resource function foo(string b) {
    }

    resource function bar(string b) {
    }
}

public type ABC object {

    *AbstractListener;

    public function __start() returns error?{
        error e = error("startError");
        return e;
    }

    public function __stop() returns error? {
        return ();
    }

    public function __attach(service s, string? name = ()) returns error? {
        return ();
    }
};

function test1 (){

}
