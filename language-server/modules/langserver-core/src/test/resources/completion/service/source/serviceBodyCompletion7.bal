import ballerina/lang.'object as lang;

public listener Listener lis = new();

service ser on lis {
    resource function res(int intVal) returns string {
        return "";
    }

    
}

type Listener object {
    *lang:Listener;

    public function init() {
    }

    public function attach(service s, string? name = ()) returns error? {
    }

    public function 'start() returns error? {
    }

    public function gracefulStop() returns error? {
        return ();
    }

    public function immediateStop() returns error? {
        return ();
    }
};
