listener ABC ep = new;

service on ep {


    resource function get foo(string b) {
    }

    resource function get bar(string b) {
    }
}

public class ABC {

    public function 'start() returns error?{
        error e = error("startError");
        panic e;
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

function test1 (){

}
