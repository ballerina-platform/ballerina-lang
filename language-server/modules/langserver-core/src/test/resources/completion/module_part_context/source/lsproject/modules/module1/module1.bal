public type Service service object {
    remote function method1();
    function method2() returns string|error;
    function method3(MyType|int arg1) returns MyType;
};

public class Listener {

    public function attach(Service s, string|string[]? name = ()) returns error? {
          
    }

    public function detach(Service s) returns error? {

    }

    public function 'start() returns error? {

    }

    public function gracefulStop() returns error? {

    }

    public function immediateStop() returns error? {

    }
}

public type MyType record {|string a;|};
