import test/proj;

@proj:FConfig {
    value: "routerFunc"
}
public function routerFunc() {
}

@proj:SConfig {
    value: "routerService"
}
service /routerService on ep {

    @proj:SConfig  {
        value: "route"
    }
    @proj:FConfig {
        value: "route"
    }
    resource function get route() {
    }

    resource function get routeRes() {
    }
}

service /routerService2 on ep {

    @proj:FConfig {
        value: "route"
    }
    resource function get route2() {

    }
}

@proj:TConfig {
    value: "routeCon"
}
public client class routeCon {

    @proj:FConfig {
        value: "route"
    }
    remote function getRoutes() {

    }
}

@proj:TConfig {
    value: "RouteConfig"
}
type RouteConfig record {

};

@proj:TConfig {
    value: "Employee"
}
type Employee record {
    string name;
    int age;
    string address;
};

type Person record {
    string firstName;
    string lastName;
    int age;
    string city;
    string street;
};


// ---- Helper definitions.
public class ABC {

    public function init() {
    }

    public function 'start() returns error? {
    }

    public function gracefulStop() returns error? {
    }

    public function immediateStop() returns error? {
    }

    public function attach(service object {} s, string|string[]? name = ()) returns error? {
    }

    public function detach(service object {} s) returns error? {
    }
}

public listener ABC ep = new ABC();
