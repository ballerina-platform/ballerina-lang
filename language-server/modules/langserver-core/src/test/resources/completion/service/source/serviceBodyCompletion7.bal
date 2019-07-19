service ser on lis {
    resource function res(int intVal) returns string {
        return "";
    }

    
}

type Listener object {
    *AbstractListener;

    public function __init() {
    }

    public function __attach(service s, string? name = ()) returns error? {
    }

    public function __start() returns error? {
    }

    public function __stop() returns error? {
    }
};
