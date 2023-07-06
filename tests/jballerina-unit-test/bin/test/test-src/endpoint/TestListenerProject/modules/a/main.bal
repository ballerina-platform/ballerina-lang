int startCount = 0;
int attachCount = 0;

public function getStartAndAttachCount() returns string {
    return attachCount.toString() + "_" + startCount.toString();
}

public class ABC {

    public function init(){
    }

    public function 'start() returns error? {
        startCount += 1;
    }

    public function gracefulStop() returns error? {
        return ();
    }

    public function immediateStop() returns error? {
        return ();
    }

    public function attach(service object {} s, string[]|string? name = ()) returns error? {
        attachCount += 1;
    }

    public function detach(service object {} s) returns error? {
    }
}

public listener ABC ep = new ABC();

service /Sample1 on ep {

    resource function get foo(string b) {
    }

    resource function get bar(string b) {
    }
}
