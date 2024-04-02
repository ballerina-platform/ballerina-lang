service / on new Listener() {

    resource function post h2(string[] arr) {
        _ = start callFunction(arr.clone());
    }

    resource function post h3(string[] arr) {
        string[] newArray = [...arr];
        _ = start callFunction(newArray);
    }
}

isolated function callFunction(string[] arr) {
}

class Listener {
    public function attach(service object {} s, string|string[]? name = ()) returns error? {
        return;
    }

    public function detach(service object {} s) returns error? {
        return;
    }

    public function 'start() returns error? {
        return;
    }

    public function gracefulStop() returns error? {
        return;
    }

    public function immediateStop() returns error? {
        return;
    }
}
