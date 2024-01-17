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

    public function attach(Service s, string[]|string? name = ()) returns error? {
        return self.register(s, name);
    }

    public function detach(Service s) returns error? {
        return ();
    }

    public function init(int port) returns error?{
    }

    public function initEndpoint() returns error? {
        return ();
    }

    function register(Service s, string[]|string? name) returns error? {
        return ();
    }

    function startEndpoint() returns error? {
        return ();
    }
}

public type Service distinct service object {

};
