public type AnnotationType record {|
    string foo;
    int bar?;
|};

public annotation AnnotationType serviceResourceFunctionAnnotation on object function;

service on new Listener(9092) {
    @serviceResourceFunctionAnnotation {
        
    }
    resource function post emptyResource() {

    }
}

public class Listener {

    private int port = 0;

    public function 'start() returns error? {
        return self.startEndpoint();
    }

    public function gracefulStop() returns error? {
        return ();
    }

    public function immediateStop() returns error? {
        error err = error("not implemented");
        return err;
    }

    public function attach(service object {} s, string[]|string? name = ()) returns error? {
        return self.register(s, name);
    }

    public function detach(service object {} s) returns error? {
        return ();
    }

    public function init(int port) {
    }

    public function initEndpoint() returns error? {
        return ();
    }

    function register(service object {} s, string[]|string? name) returns error? {
        return ();
    }

    function startEndpoint() returns error? {
        return ();
    }
}
