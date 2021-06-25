import ballerina/module1 as mod;
import ballerina/lang.'object as lang;

public listener m

public class Listener {

    *lang:Listener;

    private int port = 0;

    public function 'start() returns error? {
        return self.startEndpoint();
    }

    public function gracefulStop() returns error? {
        return self.gracefulStop();
    }

    public function immediateStop() returns error? {
        error err = error("not implemented");
        return err;
    }

    public function attach(service s, string? name = ()) returns error? {
        return self.register(s, name);
    }

    public function __detach(service s) returns error? {
        return self.detach(s);
    }

    public function init(int port) {
    }
    
    public function initEndpoint() returns error? {
        return ();
    }

    function register(service s, string? name) returns error? {
        return ();
    }

    function startEndpoint() returns error? {
        return ();
    }

    function gracefulStop() returns error? {
        return ();
    }

    function detach(service s) returns error? {
        return ();
    }
};
