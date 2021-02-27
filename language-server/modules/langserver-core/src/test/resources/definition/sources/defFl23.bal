import ballerina/lang.'object as lang;

public listener TestListener testListener = new();

function testFunction() {
	string listnerToStr = testListener.toString();
}

# Represents the shape expected from all listeners.
public type TestListener object {

    *lang:Listener;
    
    public function 'start() returns error? {
        return self.start();
    }

    public function gracefulStop() returns error? {
        return ();
    }
    
    public function immediateStop() returns error? {
        return self.stop();
    }
    
    public function attach(service s, string? name = ()) returns error? {
        return self.register(s, name);
    }

    public function __detach(service s) returns error? {
    }

    function initEndpoint() returns error? = external;

    function register(service serviceType, string? name) returns error? = external;

    function start() returns error? = external;

    function stop() returns error? = external;
};