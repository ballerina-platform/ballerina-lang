type UserRegistration record {
    string name;
    string email;
};

class Obj {
    int x = 1;
    function fn() {
    }
}

service / on new Listener() {

    resource function get h1(UserRegistration user) {
        Obj obj = new;
        _ = start sendEmailNotification(getObj(obj));
    }

    resource function post h2(string[] arr) {
        _ = start callFunction(arr.clone());
    }

    resource function post h3(string[] arr) {
        string[] newArray = [...arr];
        _ = start callFunction(newArray);
    }
}

function getObj(Obj obj) returns Obj => obj;

isolated function callFunction(string[] arr) {
}

isolated function sendEmailNotification(object {} ob) returns error? {
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
