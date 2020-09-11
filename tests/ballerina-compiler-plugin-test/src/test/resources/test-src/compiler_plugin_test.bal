import testOrg/functions;
import testOrg/services;
import testOrg/types;
import ballerina/lang.'object;

@functions:Config {
    value: "routerFunc"
}
public function routerFunc() {
}

@services:Config {
    value: "routerService"
}
service routerService on ep {

    @services:Config  {
        value: "route"
    }
    @functions:Config {
        value: "route"
    }
    resource function route() {
    }

    resource function routeRes() {
    }
}

service routerService2 on ep {

    @functions:Config {
        value: "route"
    }
    resource function route2() {

    }
}

@types:Config {
    value: "routeCon"
}
public client class routeCon {

    @functions:Config {
        value: "route"
    }
    remote function getRoutes() {

    }
}

@types:Config {
    value: "RouteConfig"
}
type RouteConfig record {

};

@types:Config {
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

    *'object:Listener;

    public function init() {
    }

    public function __start() returns error? {
    }

    public function __gracefulStop() returns error? {
    }

    public function __immediateStop() returns error? {
    }

    public function __attach(service s, string? name = ()) returns error? {
    }

    public function __detach(service s) returns error? {
    }
}

public listener ABC ep = new ABC();
