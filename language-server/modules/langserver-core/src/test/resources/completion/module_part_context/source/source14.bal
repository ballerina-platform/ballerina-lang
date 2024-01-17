import ballerina/module1;


public type Service service object {
    remote function method1();
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

public function main() {
    
}

serv