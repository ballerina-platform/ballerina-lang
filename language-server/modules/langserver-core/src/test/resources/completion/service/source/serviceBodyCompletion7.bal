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

    public function __attach(service s, string? name = ()) returns error? {
    }

    public function __start() returns error? {
    }

    public function __gracefulStop() returns error? {
        return ();
    }

    public function __immediateStop() returns error? {
        return ();
    }
};
