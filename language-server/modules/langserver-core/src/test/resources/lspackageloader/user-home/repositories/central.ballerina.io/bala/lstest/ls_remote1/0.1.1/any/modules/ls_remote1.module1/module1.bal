# Returns the string `Hello` with the input string name.
#
# + name - name as a string
# + return - "Hello, " with the input string name
public function hello(string name) returns string {
    if !(name is "") {
        return "Hello, " + name;
    }
    return "Hello, World!";
}

# Sample Listener.
public class Module1Listener {

    private int port = 9090;

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
        self.port = port;
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
