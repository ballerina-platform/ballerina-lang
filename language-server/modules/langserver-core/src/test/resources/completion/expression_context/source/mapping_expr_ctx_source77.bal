public type Key record {|
    string privateKey;
    string pubicCert;
|};

public type SecureSocket record {|
    Key key;
    string protocol;
    string auth?;
|};

public type Config record {|
    SecureSocket secureSocket?;
    int timeout = 60;
    string 'version = "2.0";
|};

public class ExtendedListener {

    private int port = 0;
    
    public function init(int port, *Config config) returns error? {

    }

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

public listener li =  new ExtendedListener(9090,  secureSocket = {});
