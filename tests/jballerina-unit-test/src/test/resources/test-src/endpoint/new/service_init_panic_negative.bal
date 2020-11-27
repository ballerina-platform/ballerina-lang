import ballerina/lang.'object;

listener ABC ep = new;

service on ep {


    resource function get foo(string b) {
    }

    resource function get bar(string b) {
    }
}

public class ABC {

    *'object:Listener;

    public function __start() returns error?{
        error e = error("startError");
        panic e;
    }

    public function __gracefulStop() returns error? {
        return ();
    }

    public function __immediateStop() returns error? {
        return ();
    }

    public function __attach(service object {} s, string[]? name = ()) returns error? {
        return ();
    }

    public function __detach(service object {} s) returns error? {
    }
}

function test1 (){

}
